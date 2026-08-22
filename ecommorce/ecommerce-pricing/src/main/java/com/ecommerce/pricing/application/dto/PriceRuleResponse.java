package com.ecommerce.pricing.application.dto;

import com.ecommerce.pricing.domain.model.PriceRule;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PriceRuleResponse(
        UUID id,
        UUID productId,
        String sku,
        PriceRule.DiscountType discountType,
        BigDecimal discountValue,
        Instant validFrom,
        Instant validUntil,
        boolean active,
        String description,
        Instant createdAt
) {}
