package com.ecommerce.catalog.domain.repository;

import com.ecommerce.catalog.domain.model.ProductStatus;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Encapsulates all supported product search / filter parameters.
 * Passed to the repository to build dynamic queries.
 */
public record ProductSearchCriteria(
        String keyword,
        UUID categoryId,
        String brand,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String currency,
        ProductStatus status
) {
    /** Default for customer-facing searches: only ACTIVE products. */
    public static ProductSearchCriteria forCustomers(String keyword, UUID categoryId,
                                                      String brand, BigDecimal minPrice,
                                                      BigDecimal maxPrice) {
        return new ProductSearchCriteria(keyword, categoryId, brand, minPrice, maxPrice,
                "USD", ProductStatus.ACTIVE);
    }

    /** Admin searches can include any status. */
    public static ProductSearchCriteria forAdmin(String keyword, UUID categoryId,
                                                  String brand, ProductStatus status) {
        return new ProductSearchCriteria(keyword, categoryId, brand, null, null, "USD", status);
    }
}
