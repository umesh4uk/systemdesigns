package com.ecommerce.cart.domain.model;

import com.ecommerce.shared.domain.valueobject.Money;
import com.ecommerce.shared.exception.DomainException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.UUID;

/**
 * Cart line item — not a JPA entity; the entire cart is serialized to Redis.
 *
 * <p>Jackson annotations on the constructor allow clean round-trip
 * serialisation without relying on default-typing or reflective field access.
 */
@Getter
public class CartItem {

    private final UUID productId;
    private final String sku;
    private final String productName;
    private int quantity;
    private Money unitPrice;      // price locked at the time the item was added/validated
    private String imageUrl;

    @JsonCreator
    public CartItem(
            @JsonProperty("productId")   UUID productId,
            @JsonProperty("sku")         String sku,
            @JsonProperty("productName") String productName,
            @JsonProperty("quantity")    int quantity,
            @JsonProperty("unitPrice")   Money unitPrice,
            @JsonProperty("imageUrl")    String imageUrl) {
        if (quantity <= 0) throw new DomainException("Cart item quantity must be positive");
        this.productId   = productId;
        this.sku         = sku;
        this.productName = productName;
        this.quantity    = quantity;
        this.unitPrice   = unitPrice;
        this.imageUrl    = imageUrl;
    }

    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) throw new DomainException("Quantity must be positive");
        this.quantity = newQuantity;
    }

    public void refreshPrice(Money currentPrice) {
        this.unitPrice = currentPrice;
    }

    public Money getLineTotal() {
        return unitPrice.multiply(quantity);
    }
}
