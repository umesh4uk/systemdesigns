package com.ecommerce.inventory.domain.event;

import com.ecommerce.shared.domain.event.BaseDomainEvent;
import java.util.UUID;

public final class StockReleasedEvent extends BaseDomainEvent {
    private final UUID inventoryItemId;
    private final String sku;
    private final int quantity;
    private final UUID orderId;

    public StockReleasedEvent(UUID inventoryItemId, String sku, int quantity, UUID orderId) {
        super();
        this.inventoryItemId = inventoryItemId;
        this.sku = sku;
        this.quantity = quantity;
        this.orderId = orderId;
    }

    @Override public String eventType() { return "inventory.stock.released"; }
    public UUID getInventoryItemId() { return inventoryItemId; }
    public String getSku()           { return sku; }
    public int getQuantity()         { return quantity; }
    public UUID getOrderId()         { return orderId; }
}
