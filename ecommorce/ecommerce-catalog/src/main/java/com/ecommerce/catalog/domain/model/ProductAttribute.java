package com.ecommerce.catalog.domain.model;

import com.ecommerce.shared.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

/**
 * Arbitrary key/value attribute for a product (e.g. Color=Red, Size=XL).
 * Stored as a separate table to allow flexible product schemas without
 * resorting to JSON blobs.
 */
@Getter
@Entity
@Table(name = "product_attributes",
        uniqueConstraints = @UniqueConstraint(name = "uq_product_attribute",
                columnNames = {"product_id", "attribute_key"}))
public class ProductAttribute extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    @Column(name = "attribute_key", nullable = false, length = 100)
    private String key;

    @Column(name = "attribute_value", nullable = false, length = 500)
    private String value;

    protected ProductAttribute() {}

    ProductAttribute(Product product, String key, String value) {
        super();
        this.product = product;
        this.key = key;
        this.value = value;
    }

    void updateValue(String newValue) {
        this.value = newValue;
    }
}
