package com.ecommerce.catalog.domain.event;

import com.ecommerce.shared.domain.event.BaseDomainEvent;

import java.util.UUID;

/**
 * Fired when a new product is created in the catalog.
 * Consumed by: Inventory (create initial stock record).
 */
public final class ProductCreatedEvent extends BaseDomainEvent {

    private final UUID productId;
    private final String sku;
    private final String name;

    public ProductCreatedEvent(UUID productId, String sku, String name) {
        super();
        this.productId = productId;
        this.sku = sku;
        this.name = name;
    }

    @Override public String eventType() { return "catalog.product.created"; }

    public UUID getProductId() { return productId; }
    public String getSku()     { return sku; }
    public String getName()    { return name; }
}
