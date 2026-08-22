package com.ecommerce.cart.application.service;

import com.ecommerce.cart.application.dto.AddCartItemRequest;
import com.ecommerce.cart.application.dto.CartResponse;
import com.ecommerce.cart.domain.model.Cart;
import com.ecommerce.cart.domain.model.CartItem;
import com.ecommerce.cart.infrastructure.redis.CartRedisRepository;
import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.inventory.application.service.InventoryService;
import com.ecommerce.pricing.application.service.PricingService;
import com.ecommerce.promotion.application.service.CouponService;
import com.ecommerce.shared.domain.valueobject.Money;
import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRedisRepository cartRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final PricingService pricingService;
    private final CouponService couponService;

    private static final String DEFAULT_CURRENCY = "USD";

    public CartResponse getCart(UUID customerId) {
        Cart cart = getOrCreateCart(customerId);
        return toResponse(cart);
    }

    public CartResponse addItem(UUID customerId, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(customerId);

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.productId()));

        if (!product.isAvailable()) {
            throw new BusinessRuleException("PRODUCT_UNAVAILABLE",
                    "Product is not available: " + product.getSku());
        }

        // Validate stock
        int available = inventoryService.getAvailableQuantity(product.getSku());
        int currentQty = cart.getItemList().stream()
                .filter(i -> i.getProductId().equals(request.productId()))
                .mapToInt(CartItem::getQuantity).sum();
        if (available < currentQty + request.quantity()) {
            throw new BusinessRuleException("INSUFFICIENT_STOCK",
                    "Only " + available + " units available for " + product.getSku());
        }

        // Resolve current price
        Money effectivePrice = pricingService.resolveEffectivePrice(
                product.getId(), product.getBasePrice());

        String imageUrl = product.getImages().stream()
                .filter(i -> i.isPrimary()).map(i -> i.getUrl()).findFirst().orElse(null);

        CartItem item = new CartItem(product.getId(), product.getSku(),
                product.getName(), request.quantity(), effectivePrice, imageUrl);
        cart.addItem(item);
        cartRepository.save(cart);
        return toResponse(cart);
    }

    public CartResponse updateItemQuantity(UUID customerId, UUID productId, int quantity) {
        Cart cart = loadCart(customerId);

        // Validate stock for new quantity
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        int available = inventoryService.getAvailableQuantity(product.getSku());
        if (available < quantity) {
            throw new BusinessRuleException("INSUFFICIENT_STOCK",
                    "Only " + available + " units available");
        }

        cart.updateItemQuantity(productId, quantity);
        cartRepository.save(cart);
        return toResponse(cart);
    }

    public CartResponse removeItem(UUID customerId, UUID productId) {
        Cart cart = loadCart(customerId);
        cart.removeItem(productId);
        cartRepository.save(cart);
        return toResponse(cart);
    }

    public CartResponse applyCoupon(UUID customerId, String couponCode) {
        Cart cart = loadCart(customerId);
        Money subtotal = cart.getSubtotal();
        // customerUsages = 0 for simplicity; real impl would query order history
        var result = couponService.applyCoupon(couponCode, subtotal, 0);
        cart.applyCoupon(couponCode, result.discountAmount());
        cartRepository.save(cart);
        return toResponse(cart);
    }

    public CartResponse removeCoupon(UUID customerId) {
        Cart cart = loadCart(customerId);
        cart.clearCoupon();
        cartRepository.save(cart);
        return toResponse(cart);
    }

    public void clearCart(UUID customerId) {
        cartRepository.delete(customerId);
    }

    // ------------------------------------------------------------------ helpers

    private Cart getOrCreateCart(UUID customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElse(new Cart(customerId, DEFAULT_CURRENCY));
    }

    private Cart loadCart(UUID customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", customerId));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartResponse.CartItemResponse> items = cart.getItemList().stream()
                .map(i -> new CartResponse.CartItemResponse(
                        i.getProductId(), i.getSku(), i.getProductName(),
                        i.getQuantity(), i.getUnitPrice().getAmount(),
                        i.getLineTotal().getAmount(), i.getImageUrl()))
                .toList();

        BigDecimal subtotal = cart.getSubtotal().getAmount();
        BigDecimal discount = cart.getCouponDiscount() != null
                ? cart.getCouponDiscount().getAmount() : BigDecimal.ZERO;
        BigDecimal total = cart.getTotal().getAmount();

        return new CartResponse(cart.getCartId(), cart.getCustomerId(),
                items, subtotal, discount, total, cart.getCurrency(), cart.getAppliedCouponCode());
    }
}
