package com.ecommerce.promotion.application.dto;

import com.ecommerce.promotion.domain.model.DiscountType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public record CouponRequest(
        @NotBlank @Size(max = 50) String code,
        @Size(max = 255) String description,
        @NotNull DiscountType discountType,
        @NotNull @DecimalMin("0.01") BigDecimal discountValue,
        @DecimalMin("0.00") BigDecimal minimumOrderAmount,
        @DecimalMin("0.00") BigDecimal maximumDiscountAmount,
        @NotNull Instant validFrom,
        Instant validUntil,
        @Min(0) int maxUsageCount,
        @Min(0) int maxUsagePerCustomer,
        @NotBlank @Size(min = 3, max = 3) String currency
) {}
