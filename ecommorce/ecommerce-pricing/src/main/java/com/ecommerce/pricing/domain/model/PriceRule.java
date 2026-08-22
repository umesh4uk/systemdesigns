package com.ecommerce.pricing.domain.model;

import com.ecommerce.shared.domain.model.AggregateRoot;
import com.ecommerce.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A price rule that can override the catalog base price for a product.
 * Supports flat discount (FIXED) or percentage discount (PERCENTAGE).
 * Rules are time-bounded and product-scoped.
 */
@Getter
@Entity
@Table(name = "price_rules")
public class PriceRule extends AggregateRoot {

    public enum DiscountType { FIXED, PERCENTAGE }

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "sku", nullable = false, length = 64)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 15)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal discountValue;

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_until")
    private Instant validUntil;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "description", length = 255)
    private String description;

    protected PriceRule() {}

    public static PriceRule create(UUID productId, String sku, DiscountType discountType,
                                    BigDecimal discountValue, Instant validFrom,
                                    Instant validUntil, String description) {
        validate(discountType, discountValue);
        PriceRule rule = new PriceRule();
        rule.productId     = productId;
        rule.sku           = sku.toUpperCase();
        rule.discountType  = discountType;
        rule.discountValue = discountValue;
        rule.validFrom     = validFrom;
        rule.validUntil    = validUntil;
        rule.description   = description;
        return rule;
    }

    public boolean isActiveAt(Instant at) {
        if (!active) return false;
        if (at.isBefore(validFrom)) return false;
        if (validUntil != null && at.isAfter(validUntil)) return false;
        return true;
    }

    public void deactivate() { this.active = false; }

    private static void validate(DiscountType type, BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0)
            throw new DomainException("Discount value must be non-negative");
        if (type == DiscountType.PERCENTAGE && value.compareTo(new BigDecimal("100")) > 0)
            throw new DomainException("Percentage discount cannot exceed 100");
    }
}
