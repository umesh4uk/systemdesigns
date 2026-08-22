package com.ecommerce.order.application.service;

import com.ecommerce.cart.domain.model.Cart;
import com.ecommerce.cart.domain.model.CartItem;
import com.ecommerce.cart.infrastructure.redis.CartRedisRepository;
import com.ecommerce.identity.domain.model.Customer;
import com.ecommerce.identity.domain.model.CustomerAddress;
import com.ecommerce.identity.domain.repository.CustomerRepository;
import com.ecommerce.inventory.application.service.InventoryService;
import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.dto.PlaceOrderRequest;
import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.model.OrderStatus;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.promotion.application.service.CouponService;
import com.ecommerce.shared.api.response.PageResponse;
import com.ecommerce.shared.domain.valueobject.Money;
import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final CartRedisRepository cartRepository;
    private final InventoryService inventoryService;
    private final CouponService couponService;
    private final ApplicationEventPublisher eventPublisher;

    private static final String DEFAULT_CURRENCY = "USD";

    @Transactional
    public OrderResponse placeOrder(UUID customerId, PlaceOrderRequest request) {
        // Load customer and validate active
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
        if (!customer.isActive()) {
            throw new BusinessRuleException("CUSTOMER_NOT_ACTIVE", "Customer account is not active");
        }

        // Load cart
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new BusinessRuleException("EMPTY_CART", "Cart is empty or expired"));
        if (cart.isEmpty()) {
            throw new BusinessRuleException("EMPTY_CART", "Cannot place an order with an empty cart");
        }

        // Resolve addresses
        CustomerAddress shippingAddr = findAddress(customer, request.shippingAddressId());
        CustomerAddress billingAddr  = findAddress(customer, request.billingAddressId());

        // Build order items, reserve stock
        List<Order.OrderItemData> orderItems = cart.getItemList().stream().map(item -> {
            inventoryService.reserveStock(item.getSku(), item.getQuantity(), null);
            return new Order.OrderItemData(item.getProductId(), item.getSku(),
                    item.getProductName(), item.getQuantity(), item.getUnitPrice());
        }).toList();

        // Apply coupon discount — use the discount already computed and stored in the cart
        // to avoid double-incrementing coupon usageCount.
        // If a new coupon code was passed at checkout (not via cart), apply it now.
        Money discount = Money.of("0", DEFAULT_CURRENCY);
        String couponCode = request.couponCode();
        if (couponCode != null && !couponCode.equals(cart.getAppliedCouponCode())) {
            // Fresh coupon at checkout (overrides cart coupon) — apply and increment usage
            var result = couponService.applyCoupon(couponCode, cart.getSubtotal(), 0);
            discount = result.discountAmount();
        } else if (cart.getAppliedCouponCode() != null) {
            // Coupon was applied at cart stage — reuse the already-computed discount
            couponCode = cart.getAppliedCouponCode();
            discount = cart.getCouponDiscount() != null ? cart.getCouponDiscount()
                    : Money.of("0", DEFAULT_CURRENCY);
        }

        Money shipping = Money.of("0", DEFAULT_CURRENCY); // simplified

        Order order = Order.place(customerId, orderItems,
                shippingAddr.getAddress(), billingAddr.getAddress(),
                discount, shipping, couponCode, DEFAULT_CURRENCY);

        Order saved = orderRepository.save(order);
        publishEvents(saved);

        // Clear cart after successful placement
        cartRepository.delete(customerId);

        log.info("Order placed: orderNumber={}, customerId={}", saved.getOrderNumber(), customerId);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId, UUID customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        // Ensure ownership
        if (!order.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException("Order", orderId);
        }
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getCustomerOrders(UUID customerId, Pageable pageable) {
        return PageResponse.from(
                orderRepository.findByCustomerId(customerId, pageable).map(this::toResponse));
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId, UUID customerId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (!order.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException("Order", orderId);
        }
        order.cancel(reason);
        // Release stock reservations
        order.getItems().forEach(item ->
                inventoryService.releaseReservation(item.getSku(), item.getQuantity(), orderId));
        Order saved = orderRepository.save(order);
        publishEvents(saved);
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse requestReturn(UUID orderId, UUID customerId) {
        Order order = loadAndOwn(orderId, customerId);
        order.requestReturn();
        return toResponse(orderRepository.save(order));
    }

    /**
     * Called by {@link com.ecommerce.order.application.listener.PaymentEventListener}
     * after a {@code PaymentCompletedEvent}. Transitions the order from
     * PAYMENT_PENDING → CONFIRMED. Idempotent: if the order is already CONFIRMED,
     * no exception is thrown.
     */
    @Transactional
    public void confirmOrderAfterPayment(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            log.debug("Order already confirmed — skipping. orderId={}", orderId);
            return;
        }
        // If still CREATED, first transition to PAYMENT_PENDING then CONFIRMED
        if (order.getStatus() == OrderStatus.CREATED) {
            order.markPaymentPending();
        }
        order.confirm();
        Order saved = orderRepository.save(order);
        publishEvents(saved);
        log.info("Order confirmed after payment. orderId={}, orderNumber={}",
                saved.getId(), saved.getOrderNumber());
    }

    // Admin operations
    @Transactional
    public OrderResponse updateStatus(UUID orderId, OrderStatus newStatus, String trackingNumber) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        switch (newStatus) {
            case CONFIRMED       -> order.confirm();
            case PROCESSING      -> order.startProcessing();
            case SHIPPED         -> order.ship(trackingNumber);
            case DELIVERED       -> order.deliver();
            case RETURNED        -> order.processReturn();
            case REFUNDED        -> order.refund();
            case PAYMENT_PENDING -> order.markPaymentPending();
            case PAYMENT_FAILED  -> order.markPaymentFailed();
            default -> throw new BusinessRuleException("INVALID_STATUS_UPDATE",
                    "Status " + newStatus + " cannot be set via this operation");
        }
        Order saved = orderRepository.save(order);
        publishEvents(saved);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<OrderResponse> getOrdersByStatus(
            OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAllOrders(Pageable pageable) {
        return PageResponse.from(
                orderRepository.findAll(pageable).map(this::toResponse));
    }

    // ------------------------------------------------------------------ helpers

    private Order loadAndOwn(UUID orderId, UUID customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (!order.getCustomerId().equals(customerId))
            throw new ResourceNotFoundException("Order", orderId);
        return order;
    }

    private CustomerAddress findAddress(Customer customer, UUID addressId) {
        return customer.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));
    }

    private void publishEvents(Order order) {
        order.getDomainEvents().forEach(eventPublisher::publishEvent);
        order.clearDomainEvents();
    }

    private OrderResponse toResponse(Order o) {
        List<OrderResponse.OrderItemResponse> items = o.getItems().stream()
                .map(i -> new OrderResponse.OrderItemResponse(
                        i.getProductId(), i.getSku(), i.getProductName(),
                        i.getQuantity(), i.getUnitPrice(),
                        i.getLineTotal().getAmount(), i.getCurrency()))
                .toList();
        return new OrderResponse(o.getId(), o.getOrderNumber(), o.getCustomerId(),
                o.getStatus(), items,
                o.getSubtotalAmount(), o.getDiscountAmount(),
                o.getShippingAmount(), o.getTotalAmount(),
                o.getCurrency(), o.getCouponCode(), o.getTrackingNumber(),
                o.getCancellationReason(), o.getCreatedAt());
    }
}
