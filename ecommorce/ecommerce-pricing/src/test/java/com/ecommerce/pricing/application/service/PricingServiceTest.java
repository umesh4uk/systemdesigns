package com.ecommerce.pricing.application.service;

import com.ecommerce.pricing.domain.model.PriceRule;
import com.ecommerce.pricing.domain.repository.PriceRuleRepository;
import com.ecommerce.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PricingService}.
 * Repository is mocked — only the price-resolution logic is under test.
 */
@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    PriceRuleRepository priceRuleRepository;

    @InjectMocks
    PricingService pricingService;

    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final Money BASE_100  = Money.of("100.00", "USD");

    // ── helper: build a PriceRule via reflection-free factory ────────────────

    private PriceRule percentageRule(BigDecimal pct) {
        return PriceRule.create(
                PRODUCT_ID, "TEST-001",
                PriceRule.DiscountType.PERCENTAGE, pct,
                Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now().plus(7, ChronoUnit.DAYS),
                "test rule");
    }

    private PriceRule fixedRule(BigDecimal amount) {
        return PriceRule.create(
                PRODUCT_ID, "TEST-001",
                PriceRule.DiscountType.FIXED, amount,
                Instant.now().minus(1, ChronoUnit.HOURS),
                Instant.now().plus(7, ChronoUnit.DAYS),
                "test rule");
    }

    // ── resolveEffectivePrice ─────────────────────────────────────────────────

    @Nested
    class ResolveEffectivePrice {

        @Test
        void should_return_base_price_when_no_active_rules() {
            when(priceRuleRepository.findActiveRulesForProduct(eq(PRODUCT_ID), any()))
                    .thenReturn(List.of());

            Money effective = pricingService.resolveEffectivePrice(PRODUCT_ID, BASE_100);

            assertThat(effective).isEqualTo(BASE_100);
        }

        @Test
        void should_apply_single_percentage_rule() {
            when(priceRuleRepository.findActiveRulesForProduct(eq(PRODUCT_ID), any()))
                    .thenReturn(List.of(percentageRule(new BigDecimal("20"))));

            Money effective = pricingService.resolveEffectivePrice(PRODUCT_ID, BASE_100);

            // 20% off $100 → $80 effective price
            assertThat(effective.getAmount()).isEqualByComparingTo("80.00");
        }

        @Test
        void should_apply_single_fixed_rule() {
            when(priceRuleRepository.findActiveRulesForProduct(eq(PRODUCT_ID), any()))
                    .thenReturn(List.of(fixedRule(new BigDecimal("15.00"))));

            Money effective = pricingService.resolveEffectivePrice(PRODUCT_ID, BASE_100);

            // $15 off $100 → $85 effective price
            assertThat(effective.getAmount()).isEqualByComparingTo("85.00");
        }

        @Test
        void should_pick_most_discounted_rule_when_multiple_active() {
            // 10% → $90, 30% → $70, $25 fixed → $75
            // Most discounted = 30% → $70
            List<PriceRule> rules = List.of(
                    percentageRule(new BigDecimal("10")),
                    percentageRule(new BigDecimal("30")),
                    fixedRule(new BigDecimal("25.00")));

            when(priceRuleRepository.findActiveRulesForProduct(eq(PRODUCT_ID), any()))
                    .thenReturn(rules);

            Money effective = pricingService.resolveEffectivePrice(PRODUCT_ID, BASE_100);

            assertThat(effective.getAmount()).isEqualByComparingTo("70.00");
        }

        @Test
        void should_clamp_fixed_discount_to_zero() {
            // Fixed $200 off $100 → effective price $0 (not negative)
            when(priceRuleRepository.findActiveRulesForProduct(eq(PRODUCT_ID), any()))
                    .thenReturn(List.of(fixedRule(new BigDecimal("200.00"))));

            Money effective = pricingService.resolveEffectivePrice(PRODUCT_ID, BASE_100);

            assertThat(effective.getAmount()).isEqualByComparingTo("0.00");
        }

        @Test
        void should_apply_100_percent_discount_to_zero() {
            when(priceRuleRepository.findActiveRulesForProduct(eq(PRODUCT_ID), any()))
                    .thenReturn(List.of(percentageRule(new BigDecimal("100"))));

            Money effective = pricingService.resolveEffectivePrice(PRODUCT_ID, BASE_100);

            assertThat(effective.getAmount()).isEqualByComparingTo("0.00");
        }

        @Test
        void should_preserve_currency_in_result() {
            Money eurBase = Money.of("100.00", "EUR");
            when(priceRuleRepository.findActiveRulesForProduct(eq(PRODUCT_ID), any()))
                    .thenReturn(List.of());

            Money effective = pricingService.resolveEffectivePrice(PRODUCT_ID, eurBase);

            assertThat(effective.getCurrencyCode()).isEqualTo("EUR");
        }
    }

    // ── getEffectivePriceDetails ──────────────────────────────────────────────

    @Nested
    class GetEffectivePriceDetails {

        @Test
        void should_report_no_active_rule_when_base_equals_effective() {
            when(priceRuleRepository.findActiveRulesForProduct(eq(PRODUCT_ID), any()))
                    .thenReturn(List.of());

            var details = pricingService.getEffectivePriceDetails(PRODUCT_ID, BASE_100);

            assertThat(details.hasActiveRule()).isFalse();
            assertThat(details.discountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(details.effectivePrice()).isEqualByComparingTo("100.00");
        }

        @Test
        void should_report_saving_correctly() {
            when(priceRuleRepository.findActiveRulesForProduct(eq(PRODUCT_ID), any()))
                    .thenReturn(List.of(percentageRule(new BigDecimal("25"))));

            var details = pricingService.getEffectivePriceDetails(PRODUCT_ID, BASE_100);

            assertThat(details.hasActiveRule()).isTrue();
            assertThat(details.effectivePrice()).isEqualByComparingTo("75.00");
            assertThat(details.discountAmount()).isEqualByComparingTo("25.00");
            assertThat(details.basePrice()).isEqualByComparingTo("100.00");
        }
    }

    // ── createRule ────────────────────────────────────────────────────────────

    @Nested
    class CreateRule {

        @Test
        void should_save_and_return_new_rule() {
            PriceRule rule = percentageRule(new BigDecimal("10"));
            when(priceRuleRepository.save(any())).thenReturn(rule);

            var request = new com.ecommerce.pricing.application.dto.CreatePriceRuleRequest(
                    PRODUCT_ID, "TEST-001",
                    PriceRule.DiscountType.PERCENTAGE, new BigDecimal("10"),
                    Instant.now().minus(1, ChronoUnit.HOURS),
                    Instant.now().plus(7, ChronoUnit.DAYS),
                    "test");

            var response = pricingService.createRule(request);

            verify(priceRuleRepository, times(1)).save(any(PriceRule.class));
            assertThat(response.discountType()).isEqualTo(PriceRule.DiscountType.PERCENTAGE);
            assertThat(response.discountValue()).isEqualByComparingTo("10");
        }
    }

    // ── deactivateRule ────────────────────────────────────────────────────────

    @Nested
    class DeactivateRule {

        @Test
        void should_deactivate_existing_rule() {
            PriceRule rule = percentageRule(new BigDecimal("10"));
            when(priceRuleRepository.findById(rule.getId()))
                    .thenReturn(java.util.Optional.of(rule));
            when(priceRuleRepository.save(any())).thenReturn(rule);

            pricingService.deactivateRule(rule.getId());

            assertThat(rule.isActive()).isFalse();
            verify(priceRuleRepository).save(rule);
        }

        @Test
        void should_throw_when_rule_not_found() {
            UUID missingId = UUID.randomUUID();
            when(priceRuleRepository.findById(missingId))
                    .thenReturn(java.util.Optional.empty());

            assertThatThrownBy(() -> pricingService.deactivateRule(missingId))
                    .isInstanceOf(com.ecommerce.shared.exception.ResourceNotFoundException.class);
        }
    }
}
