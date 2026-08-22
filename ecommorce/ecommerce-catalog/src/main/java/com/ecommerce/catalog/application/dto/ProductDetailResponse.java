package com.ecommerce.catalog.application.dto;

import com.ecommerce.catalog.domain.model.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Full product detail for PDP (Product Detail Page).
 */
public record ProductDetailResponse(
        UUID id,
        String sku,
        String name,
        String description,
        String shortDescription,
        String brand,
        BigDecimal basePrice,
        String currency,
        Integer weightGrams,
        ProductStatus status,
        UUID categoryId,
        String categoryName,
        List<ImageResponse> images,
        Map<String, String> attributes,
        Instant createdAt,
        Instant updatedAt
) {
    public record ImageResponse(UUID id, String url, String altText, int displayOrder, boolean primary) {}
}
