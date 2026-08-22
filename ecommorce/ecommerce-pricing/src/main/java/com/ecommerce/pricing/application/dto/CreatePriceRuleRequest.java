package com.ecommerce.pricing.application.dto;

import com.ecommerce.pricing.domain.model.PriceRule;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreatePriceRuleRequest(
        @NotNull UUID productId,
        @NotBlank @Size(min = 3, max = 64) String sku,
        @NotNull PriceRule.DiscountType discountType,
        @NotNull @DecimalMin("0.01") BigDecimal discountValue,
        @NotNull Instant validFrom,
        Instant validUntil,
        @Size(max = 255) String description
) {}
