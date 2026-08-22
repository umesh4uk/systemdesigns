package com.ecommerce.pricing.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Effective (post-discount) price resolved for a specific product.
 */
public record EffectivePriceResponse(
        UUID productId,
        BigDecimal basePrice,
        BigDecimal effectivePrice,
        BigDecimal discountAmount,
        String currency,
        boolean hasActiveRule
) {}
