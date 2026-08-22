package com.ecommerce.pricing.application.service;

import com.ecommerce.pricing.application.dto.CreatePriceRuleRequest;
import com.ecommerce.pricing.application.dto.EffectivePriceResponse;
import com.ecommerce.pricing.application.dto.PriceRuleResponse;
import com.ecommerce.pricing.domain.model.PriceRule;
import com.ecommerce.pricing.domain.repository.PriceRuleRepository;
import com.ecommerce.shared.domain.valueobject.Money;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Application service for pricing — resolves effective prices and manages price rules.
 */
@Service
@RequiredArgsConstructor
public class PricingService {

    private final PriceRuleRepository priceRuleRepository;

    // ── read ──────────────────────────────────────────────────────────────────

    /**
     * Resolve effective price for a product at the current instant.
     * Picks the active rule that yields the lowest (most discounted) price.
     */
    @Transactional(readOnly = true)
    public Money resolveEffectivePrice(UUID productId, Money basePrice) {
        List<PriceRule> rules = priceRuleRepository.findActiveRulesForProduct(productId, Instant.now());
        if (rules.isEmpty()) return basePrice;
        return rules.stream()
                .map(rule -> applyRule(basePrice, rule))
                .min((a, b) -> a.getAmount().compareTo(b.getAmount()))
                .orElse(basePrice);
    }

    /**
     * Returns an {@link EffectivePriceResponse} showing base price, effective price,
     * and the saving for display on the product page.
     */
    @Transactional(readOnly = true)
    public EffectivePriceResponse getEffectivePriceDetails(UUID productId, Money basePrice) {
        Money effective = resolveEffectivePrice(productId, basePrice);
        boolean hasRule = effective.getAmount().compareTo(basePrice.getAmount()) < 0;
        BigDecimal saving = basePrice.getAmount().subtract(effective.getAmount());
        return new EffectivePriceResponse(
                productId,
                basePrice.getAmount(),
                effective.getAmount(),
                saving,
                basePrice.getCurrencyCode(),
                hasRule);
    }

    @Transactional(readOnly = true)
    public List<PriceRuleResponse> getRulesForProduct(UUID productId) {
        return priceRuleRepository.findByProductId(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PriceRuleResponse getRule(UUID ruleId) {
        return toResponse(priceRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("PriceRule", ruleId)));
    }

    // ── write ─────────────────────────────────────────────────────────────────

    @Transactional
    public PriceRuleResponse createRule(CreatePriceRuleRequest req) {
        PriceRule rule = PriceRule.create(
                req.productId(), req.sku(), req.discountType(),
                req.discountValue(), req.validFrom(), req.validUntil(), req.description());
        return toResponse(priceRuleRepository.save(rule));
    }

    /**
     * Keep old signature for internal callers (CartService etc.).
     */
    @Transactional
    public PriceRule createRule(UUID productId, String sku, PriceRule.DiscountType type,
                                 BigDecimal value, Instant from, Instant until, String desc) {
        return priceRuleRepository.save(
                PriceRule.create(productId, sku, type, value, from, until, desc));
    }

    @Transactional
    public void deactivateRule(UUID ruleId) {
        PriceRule rule = priceRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("PriceRule", ruleId));
        rule.deactivate();
        priceRuleRepository.save(rule);
    }

    @Transactional
    public PriceRuleResponse reactivateRule(UUID ruleId) {
        PriceRule rule = priceRuleRepository.findById(ruleId)
                .orElseThrow(() -> new ResourceNotFoundException("PriceRule", ruleId));
        // PriceRule does not have an explicit reactivate(); set active via a new rule or use direct field
        // For simplicity we create a new rule with the same params and the admin picks the date range.
        throw new com.ecommerce.shared.exception.BusinessRuleException(
                "RULE_REACTIVATION",
                "To reactivate a deactivated rule, create a new rule with the desired date range.");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Money applyRule(Money basePrice, PriceRule rule) {
        return switch (rule.getDiscountType()) {
            case PERCENTAGE -> basePrice.discountByPercent(rule.getDiscountValue());
            case FIXED -> {
                Money discount = Money.of(rule.getDiscountValue(), basePrice.getCurrencyCode());
                yield discount.isGreaterThan(basePrice)
                        ? Money.of(BigDecimal.ZERO, basePrice.getCurrencyCode())
                        : basePrice.subtract(discount);
            }
        };
    }

    private PriceRuleResponse toResponse(PriceRule r) {
        return new PriceRuleResponse(
                r.getId(), r.getProductId(), r.getSku(),
                r.getDiscountType(), r.getDiscountValue(),
                r.getValidFrom(), r.getValidUntil(),
                r.isActive(), r.getDescription(), r.getCreatedAt());
    }
}
