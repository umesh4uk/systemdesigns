package com.ecommerce.inventory.domain.event;

import com.ecommerce.shared.domain.event.BaseDomainEvent;
import java.util.UUID;

public final class StockUpdatedEvent extends BaseDomainEvent {
    private final UUID inventoryItemId;
    private final String sku;
    private final int availableQuantity;
    private final int reservedQuantity;

    public StockUpdatedEvent(UUID inventoryItemId, String sku, int available, int reserved) {
        super();
        this.inventoryItemId = inventoryItemId;
        this.sku = sku;
        this.availableQuantity = available;
        this.reservedQuantity = reserved;
    }

    @Override public String eventType() { return "inventory.stock.updated"; }
    public UUID getInventoryItemId()  { return inventoryItemId; }
    public String getSku()            { return sku; }
    public int getAvailableQuantity() { return availableQuantity; }
    public int getReservedQuantity()  { return reservedQuantity; }
}
