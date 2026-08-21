package com.ecommerce.catalog.domain.model;

public enum ProductStatus {
    /** Draft — not visible to customers. */
    DRAFT,
    /** Live and purchasable. */
    ACTIVE,
    /** Hidden from search but accessible via direct link. */
    ARCHIVED,
    /** Permanently removed from catalog. */
    DELETED
}
