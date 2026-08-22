package com.ecommerce.cart.domain.model;

import com.ecommerce.shared.domain.valueobject.Money;
import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

/**
 * Cart aggregate. Stored in Redis — serializable, no JPA.
 *
 * <p>Invariants:
 * <ul>
 *   <li>Max 50 distinct items per cart.</li>
 *   <li>Coupon code is optional; at most one active coupon.</li>
 *   <li>All items must share the same currency.</li>
 * </ul>
 *
 * <p>Jackson annotations allow field-level (de)serialization so the cart can be
 * stored in and retrieved from Redis without requiring public setters.
 */
@Getter
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cart implements Serializable {

    private static final int MAX_ITEMS = 50;

    private UUID cartId;
    private UUID customerId;
    private Map<UUID, CartItem> items = new LinkedHashMap<>();
    private String appliedCouponCode;
    private Money couponDiscount;
    private Instant createdAt;
    private Instant updatedAt;
    private String currency;

    /** Default constructor for Jackson deserialization (Redis round-trip). */
    protected Cart() {}

    public Cart(UUID customerId, String currency) {
        this.cartId     = UUID.randomUUID();
        this.customerId = customerId;
        this.currency   = currency;
        this.createdAt  = Instant.now();
        this.updatedAt  = Instant.now();
    }

    // ── mutations ─────────────────────────────────────────────────────────────

    public void addItem(CartItem item) {
        if (items.containsKey(item.getProductId())) {
            CartItem existing = items.get(item.getProductId());
            existing.updateQuantity(existing.getQuantity() + item.getQuantity());
        } else {
            if (items.size() >= MAX_ITEMS) {
                throw new BusinessRuleException("CART_FULL",
                        "Cart cannot contain more than " + MAX_ITEMS + " distinct items");
            }
            items.put(item.getProductId(), item);
        }
        touch();
    }

    public void updateItemQuantity(UUID productId, int quantity) {
        findItem(productId).updateQuantity(quantity);
        touch();
    }

    public void removeItem(UUID productId) {
        if (!items.containsKey(productId)) {
            throw new ResourceNotFoundException("CartItem", productId);
        }
        items.remove(productId);
        clearCoupon();
        touch();
    }

    public void applyCoupon(String couponCode, Money discountAmount) {
        this.appliedCouponCode = couponCode;
        this.couponDiscount    = discountAmount;
        touch();
    }

    public void clearCoupon() {
        this.appliedCouponCode = null;
        this.couponDiscount    = null;
    }

    public void clear() {
        items.clear();
        clearCoupon();
        touch();
    }

    // ── queries ───────────────────────────────────────────────────────────────

    public Money getSubtotal() {
        return items.values().stream()
                .map(CartItem::getLineTotal)
                .reduce(Money.of("0", currency), Money::add);
    }

    public Money getTotal() {
        Money subtotal = getSubtotal();
        return (couponDiscount != null) ? subtotal.subtract(couponDiscount) : subtotal;
    }

    public boolean isEmpty()  { return items.isEmpty(); }

    public List<CartItem> getItemList() {
        return Collections.unmodifiableList(new ArrayList<>(items.values()));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private CartItem findItem(UUID productId) {
        CartItem item = items.get(productId);
        if (item == null) throw new ResourceNotFoundException("CartItem", productId);
        return item;
    }

    private void touch() { this.updatedAt = Instant.now(); }
}
