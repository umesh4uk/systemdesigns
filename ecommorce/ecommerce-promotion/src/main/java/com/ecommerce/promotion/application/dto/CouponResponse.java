package com.ecommerce.promotion.application.dto;

import com.ecommerce.promotion.domain.model.DiscountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CouponResponse(
        UUID id,
        String code,
        String description,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minimumOrderAmount,
        BigDecimal maximumDiscountAmount,
        Instant validFrom,
        Instant validUntil,
        int maxUsageCount,
        int usageCount,
        boolean active
) {}
