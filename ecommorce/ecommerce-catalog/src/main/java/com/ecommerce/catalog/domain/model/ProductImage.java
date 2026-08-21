package com.ecommerce.catalog.domain.model;

import com.ecommerce.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

/**
 * A product image URL entry — owned by the Product aggregate.
 */
@Getter
@Entity
@Table(name = "product_images")
public class ProductImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    @Column(name = "url", nullable = false, length = 1024)
    private String url;

    @Column(name = "alt_text", length = 255)
    private String altText;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    protected ProductImage() {}

    ProductImage(Product product, String url, String altText, int displayOrder, boolean primary) {
        super();
        this.product = product;
        this.url = url;
        this.altText = altText;
        this.displayOrder = displayOrder;
        this.primary = primary;
    }

    void markAsPrimary() { this.primary = true; }
    void unmarkAsPrimary() { this.primary = false; }
}
