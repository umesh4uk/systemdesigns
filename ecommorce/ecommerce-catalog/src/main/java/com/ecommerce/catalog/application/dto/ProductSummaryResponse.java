package com.ecommerce.catalog.application.dto;

import com.ecommerce.catalog.domain.model.ProductStatus;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Lightweight product representation for list/search results.
 */
public record ProductSummaryResponse(
        UUID id,
        String sku,
        String name,
        String shortDescription,
        String brand,
        BigDecimal basePrice,
        String currency,
        ProductStatus status,
        String primaryImageUrl,
        String categoryName
) {}
