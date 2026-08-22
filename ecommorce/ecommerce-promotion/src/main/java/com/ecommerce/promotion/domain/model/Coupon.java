package com.ecommerce.promotion.domain.model;

import com.ecommerce.promotion.domain.event.CouponAppliedEvent;
import com.ecommerce.shared.domain.model.AggregateRoot;
import com.ecommerce.shared.domain.valueobject.Money;
import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Coupon aggregate root.
 *
 * <p>Invariants:
 * <ul>
 *   <li>Code is immutable after creation.</li>
 *   <li>usageCount never exceeds maxUsageCount (unless maxUsageCount is 0 = unlimited).</li>
 *   <li>A single customer cannot use the same coupon more than maxUsagePerCustomer times.</li>
 *   <li>Order total must meet minimumOrderAmount.</li>
 *   <li>Coupon must be within valid date range.</li>
 * </ul>
 */
@Getter
@Entity
@Table(name = "coupons",
        uniqueConstraints = @UniqueConstraint(name = "uq_coupon_code", columnNames = "code"))
public class Coupon extends AggregateRoot {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 15)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal discountValue;

    @Column(name = "minimum_order_amount", precision = 19, scale = 4)
    private BigDecimal minimumOrderAmount;

    @Column(name = "maximum_discount_amount", precision = 19, scale = 4)
    private BigDecimal maximumDiscountAmount;  // cap for percentage discounts

    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @Column(name = "valid_until")
    private Instant validUntil;

    @Column(name = "max_usage_count", nullable = false)
    private int maxUsageCount;  // 0 = unlimited

    @Column(name = "max_usage_per_customer", nullable = false)
    private int maxUsagePerCustomer;  // 0 = unlimited

    @Column(name = "usage_count", nullable = false)
    private int usageCount;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    protected Coupon() {}

    public static Coupon create(String code, String description, DiscountType discountType,
                                 BigDecimal discountValue, BigDecimal minimumOrderAmount,
                                 BigDecimal maximumDiscountAmount, Instant validFrom,
                                 Instant validUntil, int maxUsageCount,
                                 int maxUsagePerCustomer, String currency) {
        validateDiscountValue(discountType, discountValue);
        Coupon c = new Coupon();
        c.code                  = code.toUpperCase().trim();
        c.description           = description;
        c.discountType          = discountType;
        c.discountValue         = discountValue;
        c.minimumOrderAmount    = minimumOrderAmount;
        c.maximumDiscountAmount = maximumDiscountAmount;
        c.validFrom             = validFrom;
        c.validUntil            = validUntil;
        c.maxUsageCount         = maxUsageCount;
        c.maxUsagePerCustomer   = maxUsagePerCustomer;
        c.usageCount            = 0;
        c.currency              = currency;
        return c;
    }

    /**
     * Validate eligibility and compute discount amount.
     *
     * @param orderTotal     order subtotal before this coupon
     * @param customerUsages how many times this customer already used this coupon
     * @return computed discount amount
     */
    public Money apply(Money orderTotal, int customerUsages) {
        Instant now = Instant.now();

        if (!active)
            throw new BusinessRuleException("COUPON_INACTIVE", "Coupon is inactive: " + code);
        if (now.isBefore(validFrom))
            throw new BusinessRuleException("COUPON_NOT_YET_VALID", "Coupon is not yet valid: " + code);
        if (validUntil != null && now.isAfter(validUntil))
            throw new BusinessRuleException("COUPON_EXPIRED", "Coupon has expired: " + code);
        if (maxUsageCount > 0 && usageCount >= maxUsageCount)
            throw new BusinessRuleException("COUPON_EXHAUSTED", "Coupon usage limit reached: " + code);
        if (maxUsagePerCustomer > 0 && customerUsages >= maxUsagePerCustomer)
            throw new BusinessRuleException("COUPON_CUSTOMER_LIMIT",
                    "You have already used this coupon the maximum number of times");
        if (minimumOrderAmount != null &&
                orderTotal.getAmount().compareTo(minimumOrderAmount) < 0)
            throw new BusinessRuleException("COUPON_MIN_ORDER",
                    "Order total does not meet minimum amount of " + minimumOrderAmount);

        Money discount = computeDiscount(orderTotal);
        this.usageCount++;
        registerEvent(new CouponAppliedEvent(getId(), code));
        return discount;
    }

    public void deactivate() { this.active = false; }
    public void activate()   { this.active = true; }

    /**
     * Returns the discount AMOUNT (i.e. how much to subtract from the order total).
     *
     * For PERCENTAGE:  discountByPercent returns the post-discount price,
     *                  so the actual saving = orderTotal − discountedPrice.
     * For FIXED_AMOUNT: the fixed value IS the discount amount directly.
     */
    private Money computeDiscount(Money orderTotal) {
        Money discountAmount = switch (discountType) {
            case PERCENTAGE -> {
                // discountedPrice = orderTotal * (1 - pct/100)
                Money discountedPrice = orderTotal.discountByPercent(discountValue);
                // saving = what we knocked off
                yield orderTotal.subtract(discountedPrice);
            }
            case FIXED_AMOUNT -> {
                Money fixed = Money.of(discountValue, currency);
                // Cannot discount more than the order total
                yield fixed.isGreaterThan(orderTotal) ? orderTotal : fixed;
            }
        };

        // Cap percentage discounts if maximumDiscountAmount is configured
        if (discountType == DiscountType.PERCENTAGE && maximumDiscountAmount != null) {
            Money cap = Money.of(maximumDiscountAmount, currency);
            if (discountAmount.isGreaterThan(cap)) {
                discountAmount = cap;
            }
        }

        return discountAmount;
    }

    private static void validateDiscountValue(DiscountType type, BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0)
            throw new DomainException("Discount value must be positive");
        if (type == DiscountType.PERCENTAGE && value.compareTo(new BigDecimal("100")) > 0)
            throw new DomainException("Percentage discount cannot exceed 100");
    }
}
