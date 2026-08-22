package com.ecommerce.promotion.domain.model;

import com.ecommerce.promotion.domain.event.CouponAppliedEvent;
import com.ecommerce.shared.domain.valueobject.Money;
import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.DomainException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the Coupon aggregate, covering:
 * - Percentage discounts (with and without cap)
 * - Fixed-amount discounts (including clamp at order total)
 * - Eligibility guards (expired, inactive, usage exhausted, minimum order)
 * - usageCount increment on apply
 * - Domain event emission
 */
class CouponTest {

    // ── factory helpers ───────────────────────────────────────────────────────

    private static Coupon percentageCoupon(BigDecimal pct) {
        return Coupon.create(
                "PCT20", "20% off", DiscountType.PERCENTAGE, pct,
                null, null,
                Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now().plus(7, ChronoUnit.DAYS),
                0, 0, "USD");
    }

    private static Coupon fixedCoupon(BigDecimal amount) {
        return Coupon.create(
                "FIXED10", "$10 off", DiscountType.FIXED_AMOUNT, amount,
                null, null,
                Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now().plus(7, ChronoUnit.DAYS),
                0, 0, "USD");
    }

    private static Coupon expiredCoupon() {
        return Coupon.create(
                "OLD", "Expired", DiscountType.PERCENTAGE, new BigDecimal("10"),
                null, null,
                Instant.now().minus(10, ChronoUnit.DAYS),
                Instant.now().minus(1, ChronoUnit.DAYS),   // validUntil in the past
                0, 0, "USD");
    }

    private static final Money ORDER_100 = Money.of("100.00", "USD");

    // ── Percentage discounts ──────────────────────────────────────────────────

    @Nested
    class PercentageDiscounts {

        @Test
        void should_compute_20_percent_discount_correctly() {
            Coupon coupon = percentageCoupon(new BigDecimal("20"));
            Money discount = coupon.apply(ORDER_100, 0);
            // 20% of $100 = $20 discount
            assertThat(discount.getAmount()).isEqualByComparingTo("20.00");
        }

        @Test
        void should_return_full_order_total_for_100_percent_discount() {
            Coupon coupon = percentageCoupon(new BigDecimal("100"));
            Money discount = coupon.apply(ORDER_100, 0);
            assertThat(discount.getAmount()).isEqualByComparingTo("100.00");
        }

        @ParameterizedTest(name = "{0}% off $100 = ${1} discount")
        @CsvSource({"10, 10.00", "25, 25.00", "50, 50.00", "75, 75.00"})
        void should_compute_various_percentages(String pct, String expectedDiscount) {
            Coupon coupon = percentageCoupon(new BigDecimal(pct));
            Money discount = coupon.apply(ORDER_100, 0);
            assertThat(discount.getAmount()).isEqualByComparingTo(expectedDiscount);
        }

        @Test
        void should_cap_discount_at_maximum_when_configured() {
            // 50% of $100 = $50 — but cap is $30
            Coupon coupon = Coupon.create(
                    "CAPPED", "50% up to $30", DiscountType.PERCENTAGE, new BigDecimal("50"),
                    null, new BigDecimal("30.00"),
                    Instant.now().minus(1, ChronoUnit.HOURS),
                    Instant.now().plus(7, ChronoUnit.DAYS),
                    0, 0, "USD");

            Money discount = coupon.apply(ORDER_100, 0);
            assertThat(discount.getAmount()).isEqualByComparingTo("30.00");
        }

        @Test
        void should_not_cap_when_discount_is_below_maximum() {
            // 10% of $100 = $10, cap is $30 — cap should NOT apply
            Coupon coupon = Coupon.create(
                    "NOCAP", "10% up to $30", DiscountType.PERCENTAGE, new BigDecimal("10"),
                    null, new BigDecimal("30.00"),
                    Instant.now().minus(1, ChronoUnit.HOURS),
                    Instant.now().plus(7, ChronoUnit.DAYS),
                    0, 0, "USD");

            Money discount = coupon.apply(ORDER_100, 0);
            assertThat(discount.getAmount()).isEqualByComparingTo("10.00");
        }
    }

    // ── Fixed-amount discounts ────────────────────────────────────────────────

    @Nested
    class FixedAmountDiscounts {

        @Test
        void should_compute_fixed_10_discount() {
            Coupon coupon = fixedCoupon(new BigDecimal("10.00"));
            Money discount = coupon.apply(ORDER_100, 0);
            assertThat(discount.getAmount()).isEqualByComparingTo("10.00");
        }

        @Test
        void should_clamp_fixed_discount_to_order_total() {
            // $150 off a $100 order → discount = $100 (cannot go negative)
            Coupon coupon = fixedCoupon(new BigDecimal("150.00"));
            Money discount = coupon.apply(ORDER_100, 0);
            assertThat(discount.getAmount()).isEqualByComparingTo("100.00");
        }

        @Test
        void should_return_exact_order_total_for_equal_fixed_discount() {
            Coupon coupon = fixedCoupon(new BigDecimal("100.00"));
            Money discount = coupon.apply(ORDER_100, 0);
            assertThat(discount.getAmount()).isEqualByComparingTo("100.00");
        }
    }

    // ── Eligibility guards ────────────────────────────────────────────────────

    @Nested
    class EligibilityGuards {

        @Test
        void should_reject_expired_coupon() {
            Coupon coupon = expiredCoupon();
            assertThatThrownBy(() -> coupon.apply(ORDER_100, 0))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("expired");
        }

        @Test
        void should_reject_inactive_coupon() {
            Coupon coupon = percentageCoupon(new BigDecimal("10"));
            coupon.deactivate();
            assertThatThrownBy(() -> coupon.apply(ORDER_100, 0))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("inactive");
        }

        @Test
        void should_reject_when_max_usage_exhausted() {
            Coupon coupon = Coupon.create(
                    "LIMITED", "1-use", DiscountType.PERCENTAGE, new BigDecimal("10"),
                    null, null,
                    Instant.now().minus(1, ChronoUnit.HOURS),
                    Instant.now().plus(7, ChronoUnit.DAYS),
                    1, 0, "USD");       // maxUsageCount = 1

            coupon.apply(ORDER_100, 0);  // first use — OK
            // Second use: usageCount == maxUsageCount → rejected
            assertThatThrownBy(() -> coupon.apply(ORDER_100, 0))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("limit");
        }

        @Test
        void should_reject_when_customer_limit_exceeded() {
            Coupon coupon = Coupon.create(
                    "CUSTLIM", "1-per-customer", DiscountType.PERCENTAGE, new BigDecimal("10"),
                    null, null,
                    Instant.now().minus(1, ChronoUnit.HOURS),
                    Instant.now().plus(7, ChronoUnit.DAYS),
                    0, 1, "USD");       // maxUsagePerCustomer = 1

            // customerUsages = 1 already → rejected
            assertThatThrownBy(() -> coupon.apply(ORDER_100, 1))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("maximum number of times");
        }

        @Test
        void should_reject_when_order_below_minimum() {
            Coupon coupon = Coupon.create(
                    "MINORD", "$50 minimum", DiscountType.PERCENTAGE, new BigDecimal("10"),
                    new BigDecimal("50.00"), null,
                    Instant.now().minus(1, ChronoUnit.HOURS),
                    Instant.now().plus(7, ChronoUnit.DAYS),
                    0, 0, "USD");

            Money smallOrder = Money.of("30.00", "USD");
            assertThatThrownBy(() -> coupon.apply(smallOrder, 0))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("minimum amount");
        }

        @Test
        void should_reject_coupon_not_yet_valid() {
            Coupon coupon = Coupon.create(
                    "FUTURE", "Future coupon", DiscountType.PERCENTAGE, new BigDecimal("10"),
                    null, null,
                    Instant.now().plus(1, ChronoUnit.DAYS),  // validFrom in future
                    Instant.now().plus(7, ChronoUnit.DAYS),
                    0, 0, "USD");

            assertThatThrownBy(() -> coupon.apply(ORDER_100, 0))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("not yet valid");
        }
    }

    // ── Side-effects ─────────────────────────────────────────────────────────

    @Nested
    class SideEffects {

        @Test
        void should_increment_usage_count_on_apply() {
            Coupon coupon = percentageCoupon(new BigDecimal("10"));
            assertThat(coupon.getUsageCount()).isZero();
            coupon.apply(ORDER_100, 0);
            assertThat(coupon.getUsageCount()).isEqualTo(1);
            coupon.apply(ORDER_100, 0);
            assertThat(coupon.getUsageCount()).isEqualTo(2);
        }

        @Test
        void should_emit_coupon_applied_event() {
            Coupon coupon = percentageCoupon(new BigDecimal("10"));
            coupon.apply(ORDER_100, 0);
            assertThat(coupon.getDomainEvents())
                    .hasSize(1)
                    .first()
                    .isInstanceOf(CouponAppliedEvent.class);
        }

        @Test
        void should_not_emit_event_when_validation_fails() {
            Coupon coupon = expiredCoupon();
            assertThatThrownBy(() -> coupon.apply(ORDER_100, 0))
                    .isInstanceOf(BusinessRuleException.class);
            assertThat(coupon.getDomainEvents()).isEmpty();
        }

        @Test
        void should_not_change_usage_count_when_validation_fails() {
            Coupon coupon = expiredCoupon();
            assertThatThrownBy(() -> coupon.apply(ORDER_100, 0))
                    .isInstanceOf(BusinessRuleException.class);
            assertThat(coupon.getUsageCount()).isZero();
        }
    }

    // ── Construction guards ───────────────────────────────────────────────────

    @Nested
    class ConstructionGuards {

        @Test
        void should_reject_zero_discount_value() {
            assertThatThrownBy(() -> Coupon.create(
                    "ZERO", "zero", DiscountType.PERCENTAGE, BigDecimal.ZERO,
                    null, null,
                    Instant.now(), null, 0, 0, "USD"))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        void should_reject_percentage_over_100() {
            assertThatThrownBy(() -> Coupon.create(
                    "OVER", "over 100%", DiscountType.PERCENTAGE, new BigDecimal("101"),
                    null, null,
                    Instant.now(), null, 0, 0, "USD"))
                    .isInstanceOf(DomainException.class)
                    .hasMessageContaining("100");
        }

        @Test
        void should_normalise_code_to_uppercase() {
            Coupon coupon = percentageCoupon(new BigDecimal("10"));
            assertThat(coupon.getCode()).isEqualTo("PCT20");
        }
    }
}
