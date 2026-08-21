package com.ecommerce.catalog.domain.event;

import com.ecommerce.catalog.domain.model.ProductStatus;
import com.ecommerce.shared.domain.event.BaseDomainEvent;

import java.util.UUID;

public final class ProductStatusChangedEvent extends BaseDomainEvent {

    private final UUID productId;
    private final ProductStatus previousStatus;
    private final ProductStatus newStatus;

    public ProductStatusChangedEvent(UUID productId, ProductStatus previousStatus, ProductStatus newStatus) {
        super();
        this.productId = productId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }

    @Override public String eventType() { return "catalog.product.status.changed"; }

    public UUID getProductId()              { return productId; }
    public ProductStatus getPreviousStatus(){ return previousStatus; }
    public ProductStatus getNewStatus()     { return newStatus; }
}
