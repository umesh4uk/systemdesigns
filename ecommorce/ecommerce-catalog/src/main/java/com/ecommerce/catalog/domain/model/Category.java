package com.ecommerce.catalog.domain.model;

import com.ecommerce.shared.domain.model.AggregateRoot;
import com.ecommerce.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Category aggregate root. Supports an unlimited hierarchy:
 * <pre>
 * Electronics
 *   ├── Mobiles
 *   └── Laptops
 *       └── Gaming Laptops
 * </pre>
 * Slug is URL-safe and unique within the catalog.
 */
@Getter
@Entity
@Table(name = "categories",
        uniqueConstraints = @UniqueConstraint(name = "uq_category_slug", columnNames = "slug"))
public class Category extends AggregateRoot {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 150)
    private String slug;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private final List<Category> children = new ArrayList<>();

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected Category() {}

    /**
     * Factory — top-level category.
     */
    public static Category createRoot(String name, String slug, String description, int displayOrder) {
        return create(name, slug, description, null, displayOrder);
    }

    /**
     * Factory — child category.
     */
    public static Category createChild(String name, String slug, String description,
                                       Category parent, int displayOrder) {
        if (parent == null) {
            throw new DomainException("Parent category must not be null for a child category");
        }
        return create(name, slug, description, parent, displayOrder);
    }

    private static Category create(String name, String slug, String description,
                                    Category parent, int displayOrder) {
        Category c = new Category();
        c.name = validateName(name);
        c.slug = validateSlug(slug);
        c.description = description;
        c.parent = parent;
        c.displayOrder = displayOrder;
        return c;
    }

    public void update(String name, String description, String imageUrl, int displayOrder) {
        this.name = validateName(name);
        this.description = description;
        this.imageUrl = imageUrl;
        this.displayOrder = displayOrder;
    }

    public void deactivate() { this.active = false; }
    public void activate()   { this.active = true; }

    public boolean isRoot() { return parent == null; }

    public List<Category> getChildren() {
        return Collections.unmodifiableList(children);
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) throw new DomainException("Category name must not be blank");
        if (name.length() > 150) throw new DomainException("Category name too long (max 150)");
        return name.trim();
    }

    private static String validateSlug(String slug) {
        if (slug == null || !slug.matches("[a-z0-9\\-]+")) {
            throw new DomainException("Invalid category slug: " + slug
                    + ". Must be lowercase alphanumeric with hyphens.");
        }
        return slug;
    }
}
