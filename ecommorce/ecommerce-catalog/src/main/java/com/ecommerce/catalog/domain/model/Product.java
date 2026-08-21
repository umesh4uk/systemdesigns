package com.ecommerce.catalog.domain.model;

import com.ecommerce.catalog.domain.event.ProductCreatedEvent;
import com.ecommerce.catalog.domain.event.ProductStatusChangedEvent;
import com.ecommerce.shared.domain.model.AggregateRoot;
import com.ecommerce.shared.domain.valueobject.Money;
import com.ecommerce.shared.domain.valueobject.Sku;
import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.ConflictException;
import com.ecommerce.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Product aggregate root.
 *
 * <p>Invariants:
 * <ul>
 *   <li>SKU is immutable after creation and must be unique.</li>
 *   <li>Base price must be non-negative.</li>
 *   <li>A product must have at least one image before being published.</li>
 *   <li>Attribute keys are unique per product.</li>
 *   <li>Only one primary image is allowed.</li>
 * </ul>
 */
@Getter
@Entity
@Table(name = "products",
        uniqueConstraints = @UniqueConstraint(name = "uq_product_sku", columnNames = "sku"))
public class Product extends AggregateRoot {

    @Column(name = "sku", nullable = false, unique = true, length = 64)
    private String sku;

    @Column(name = "name", nullable = false, length = 300)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "brand", length = 150)
    private String brand;

    @Column(name = "base_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal basePrice;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProductStatus status;

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private final List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ProductAttribute> attributes = new ArrayList<>();

    protected Product() {}

    // ------------------------------------------------------------------ factory

    public static Product create(String sku, String name, String description,
                                  String shortDescription, Category category,
                                  String brand, Money basePrice, Integer weightGrams) {
        Product p = new Product();
        p.sku = Sku.of(sku).getValue();
        p.name = requireNonBlank(name, "name");
        p.description = description;
        p.shortDescription = shortDescription;
        p.category = category;
        p.brand = brand;
        p.setPrice(basePrice);
        p.weightGrams = weightGrams;
        p.status = ProductStatus.DRAFT;
        p.registerEvent(new ProductCreatedEvent(p.getId(), p.sku, p.name));
        return p;
    }

    // ------------------------------------------------------------------ commands

    public void updateDetails(String name, String description, String shortDescription,
                               Category category, String brand, Integer weightGrams) {
        this.name = requireNonBlank(name, "name");
        this.description = description;
        this.shortDescription = shortDescription;
        this.category = category;
        this.brand = brand;
        this.weightGrams = weightGrams;
    }

    public void updatePrice(Money newPrice) {
        if (status == ProductStatus.DELETED) {
            throw new BusinessRuleException("PRODUCT_DELETED", "Cannot update price of a deleted product");
        }
        setPrice(newPrice);
    }

    public void publish() {
        if (images.isEmpty()) {
            throw new BusinessRuleException("NO_PRODUCT_IMAGES",
                    "A product must have at least one image before publishing");
        }
        if (status == ProductStatus.DELETED) {
            throw new BusinessRuleException("PRODUCT_DELETED", "Cannot publish a deleted product");
        }
        ProductStatus previous = this.status;
        this.status = ProductStatus.ACTIVE;
        if (previous != ProductStatus.ACTIVE) {
            registerEvent(new ProductStatusChangedEvent(getId(), previous, ProductStatus.ACTIVE));
        }
    }

    public void archive() {
        if (status == ProductStatus.DELETED) {
            throw new BusinessRuleException("PRODUCT_DELETED", "Cannot archive a deleted product");
        }
        this.status = ProductStatus.ARCHIVED;
        registerEvent(new ProductStatusChangedEvent(getId(), ProductStatus.ACTIVE, ProductStatus.ARCHIVED));
    }

    public void delete() {
        this.status = ProductStatus.DELETED;
    }

    public boolean isAvailable() {
        return status == ProductStatus.ACTIVE;
    }

    public Money getBasePrice() {
        return Money.of(basePrice, currency);
    }

    // ------------------------------------------------------------------ images

    public ProductImage addImage(String url, String altText, int displayOrder) {
        requireNonBlank(url, "image URL");
        ProductImage image = new ProductImage(this, url, altText, displayOrder, images.isEmpty());
        images.add(image);
        return image;
    }

    public void setPrimaryImage(UUID imageId) {
        images.forEach(ProductImage::unmarkAsPrimary);
        images.stream()
                .filter(i -> i.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new DomainException("Image not found: " + imageId))
                .markAsPrimary();
    }

    public void removeImage(UUID imageId) {
        ProductImage target = images.stream()
                .filter(i -> i.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new DomainException("Image not found: " + imageId));
        if (target.isPrimary() && images.size() > 1) {
            throw new BusinessRuleException("PRIMARY_IMAGE_REMOVAL",
                    "Cannot remove primary image. Set another image as primary first.");
        }
        images.remove(target);
    }

    public List<ProductImage> getImages() {
        return Collections.unmodifiableList(images);
    }

    // ------------------------------------------------------------------ attributes

    public void setAttribute(String key, String value) {
        requireNonBlank(key, "attribute key");
        requireNonBlank(value, "attribute value");
        attributes.stream()
                .filter(a -> a.getKey().equalsIgnoreCase(key))
                .findFirst()
                .ifPresentOrElse(
                        a -> a.updateValue(value),
                        () -> attributes.add(new ProductAttribute(this, key.toLowerCase(), value))
                );
    }

    public void removeAttribute(String key) {
        attributes.removeIf(a -> a.getKey().equalsIgnoreCase(key));
    }

    public List<ProductAttribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    // ------------------------------------------------------------------ helpers

    private void setPrice(Money price) {
        if (price.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("Base price cannot be negative");
        }
        this.basePrice = price.getAmount();
        this.currency = price.getCurrencyCode();
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) throw new DomainException(field + " must not be blank");
        return value.trim();
    }
}
