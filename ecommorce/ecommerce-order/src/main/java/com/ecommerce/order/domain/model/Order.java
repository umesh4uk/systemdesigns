package com.ecommerce.order.domain.model;

import com.ecommerce.order.domain.event.*;
import com.ecommerce.shared.domain.model.AggregateRoot;
import com.ecommerce.shared.domain.valueobject.Address;
import com.ecommerce.shared.domain.valueobject.Money;
import com.ecommerce.shared.domain.valueobject.OrderNumber;
import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.*;

/**
 * Order aggregate root. Owns the full order lifecycle state machine.
 *
 * <p>Key invariants:
 * <ul>
 *   <li>Must have at least one item.</li>
 *   <li>Prices are captured at placement — immutable thereafter.</li>
 *   <li>Status transitions are strictly validated.</li>
 *   <li>A delivered order cannot be cancelled.</li>
 *   <li>Cancellation is only valid before SHIPPED.</li>
 * </ul>
 */
@Getter
@Entity
@Table(name = "orders")
public class Order extends AggregateRoot {

    @Column(name = "order_number", nullable = false, unique = true, length = 30)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 25)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<OrderItem> items = new ArrayList<>();

    // Shipping address snapshot — embedded so it cannot change after placement
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "addressLine1", column = @Column(name = "ship_line1")),
        @AttributeOverride(name = "addressLine2", column = @Column(name = "ship_line2")),
        @AttributeOverride(name = "city",         column = @Column(name = "ship_city")),
        @AttributeOverride(name = "state",        column = @Column(name = "ship_state")),
        @AttributeOverride(name = "postalCode",   column = @Column(name = "ship_postal_code")),
        @AttributeOverride(name = "country",      column = @Column(name = "ship_country"))
    })
    private Address shippingAddress;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "addressLine1", column = @Column(name = "bill_line1")),
        @AttributeOverride(name = "addressLine2", column = @Column(name = "bill_line2")),
        @AttributeOverride(name = "city",         column = @Column(name = "bill_city")),
        @AttributeOverride(name = "state",        column = @Column(name = "bill_state")),
        @AttributeOverride(name = "postalCode",   column = @Column(name = "bill_postal_code")),
        @AttributeOverride(name = "country",      column = @Column(name = "bill_country"))
    })
    private Address billingAddress;

    @Column(name = "subtotal_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotalAmount;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal discountAmount;

    @Column(name = "shipping_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal shippingAmount;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    protected Order() {}

    // ------------------------------------------------------------------ factory

    public static Order place(UUID customerId, List<OrderItemData> orderItems,
                               Address shippingAddress, Address billingAddress,
                               Money discount, Money shipping, String couponCode, String currency) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new DomainException("An order must contain at least one item");
        }

        Order order = new Order();
        order.orderNumber     = OrderNumber.generate().getValue();
        order.customerId      = customerId;
        order.status          = OrderStatus.CREATED;
        order.shippingAddress = shippingAddress;
        order.billingAddress  = billingAddress;
        order.currency        = currency;
        order.couponCode      = couponCode;

        // Add items — prices are locked at this point
        for (OrderItemData data : orderItems) {
            order.items.add(new OrderItem(order, data.productId(), data.sku(),
                    data.productName(), data.quantity(), data.unitPrice()));
        }

        // Compute totals
        Money subtotal = order.items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(Money.of("0", currency), Money::add);

        order.subtotalAmount = subtotal.getAmount();
        order.discountAmount = discount.getAmount();
        order.shippingAmount = shipping.getAmount();
        order.totalAmount    = subtotal.subtract(discount).add(shipping).getAmount();

        order.registerEvent(new OrderPlacedEvent(order.getId(), order.orderNumber,
                order.customerId, order.totalAmount, currency));
        return order;
    }

    // ------------------------------------------------------------------ state machine

    public void markPaymentPending() {
        transition(OrderStatus.PAYMENT_PENDING);
    }

    public void confirm() {
        transition(OrderStatus.CONFIRMED);
        registerEvent(new OrderConfirmedEvent(getId(), orderNumber, customerId));
    }

    public void markPaymentFailed() {
        transition(OrderStatus.PAYMENT_FAILED);
    }

    public void startProcessing() {
        transition(OrderStatus.PROCESSING);
    }

    public void ship(String trackingNumber) {
        transition(OrderStatus.SHIPPED);
        this.trackingNumber = trackingNumber;
        registerEvent(new OrderShippedEvent(getId(), orderNumber, customerId, trackingNumber));
    }

    public void deliver() {
        transition(OrderStatus.DELIVERED);
        registerEvent(new OrderDeliveredEvent(getId(), orderNumber, customerId));
    }

    public void cancel(String reason) {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new BusinessRuleException("ORDER_CANCELLATION_NOT_ALLOWED",
                    "Cannot cancel a shipped or delivered order");
        }
        transition(OrderStatus.CANCELLED);
        this.cancellationReason = reason;
        registerEvent(new OrderCancelledEvent(getId(), orderNumber, customerId, reason));
    }

    public void requestReturn() {
        transition(OrderStatus.RETURN_REQUESTED);
    }

    public void processReturn() {
        transition(OrderStatus.RETURNED);
    }

    public void refund() {
        transition(OrderStatus.REFUNDED);
        registerEvent(new OrderRefundedEvent(getId(), orderNumber, customerId, getTotalMoney()));
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Money getTotalMoney()    { return Money.of(totalAmount, currency); }
    public Money getSubtotalMoney() { return Money.of(subtotalAmount, currency); }
    public Money getDiscountMoney() { return Money.of(discountAmount, currency); }
    public Money getShippingMoney() { return Money.of(shippingAmount, currency); }

    public boolean isCancellable() {
        return status == OrderStatus.CREATED
                || status == OrderStatus.PAYMENT_PENDING
                || status == OrderStatus.PAYMENT_FAILED
                || status == OrderStatus.CONFIRMED
                || status == OrderStatus.PROCESSING;
    }

    private void transition(OrderStatus next) {
        status.validateTransition(next);
        this.status = next;
    }

    /** Value record for item data when placing an order. */
    public record OrderItemData(
            UUID productId, String sku, String productName,
            int quantity, Money unitPrice) {}
}
