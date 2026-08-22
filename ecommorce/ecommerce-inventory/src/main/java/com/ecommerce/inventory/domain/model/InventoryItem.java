package com.ecommerce.inventory.domain.model;

import com.ecommerce.inventory.domain.event.StockReservedEvent;
import com.ecommerce.inventory.domain.event.StockReleasedEvent;
import com.ecommerce.inventory.domain.event.StockUpdatedEvent;
import com.ecommerce.shared.domain.model.AggregateRoot;
import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.InsufficientStockException;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

/**
 * Inventory aggregate root — tracks stock for one SKU at one warehouse.
 *
 * <p>Invariants:
 * <ul>
 *   <li>availableQuantity >= 0 at all times</li>
 *   <li>reservedQuantity >= 0 at all times</li>
 *   <li>Reservation cannot exceed available quantity</li>
 *   <li>totalQuantity = availableQuantity + reservedQuantity</li>
 * </ul>
 */
@Getter
@Entity
@Table(name = "inventory_items",
        uniqueConstraints = @UniqueConstraint(name = "uq_inventory_sku_warehouse",
                columnNames = {"sku", "warehouse_id"}))
public class InventoryItem extends AggregateRoot {

    @Column(name = "sku", nullable = false, length = 64)
    private String sku;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "warehouse_id", nullable = false, length = 100)
    private String warehouseId;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Column(name = "reorder_threshold", nullable = false)
    private int reorderThreshold;

    protected InventoryItem() {}

    public static InventoryItem create(UUID productId, String sku, String warehouseId,
                                        int initialQuantity, int reorderThreshold) {
        InventoryItem item = new InventoryItem();
        item.productId = productId;
        item.sku = sku.toUpperCase();
        item.warehouseId = warehouseId;
        item.availableQuantity = initialQuantity;
        item.reservedQuantity = 0;
        item.reorderThreshold = reorderThreshold;
        return item;
    }

    /** Adds stock (receiving, restock). */
    public void addStock(int quantity) {
        if (quantity <= 0) throw new BusinessRuleException("INVALID_QUANTITY", "Quantity to add must be positive");
        this.availableQuantity += quantity;
        registerEvent(new StockUpdatedEvent(getId(), sku, availableQuantity, reservedQuantity));
    }

    /**
     * Reserve stock for an order/cart.
     * Reduces available, increases reserved — prevents overselling.
     */
    public void reserve(int quantity, UUID orderId) {
        if (quantity <= 0) throw new BusinessRuleException("INVALID_QUANTITY", "Reservation quantity must be positive");
        if (availableQuantity < quantity) {
            throw new InsufficientStockException(sku, quantity, availableQuantity);
        }
        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
        registerEvent(new StockReservedEvent(getId(), sku, quantity, orderId));
    }

    /** Confirm reservation — reduce reserved (stock has shipped). */
    public void confirmReservation(int quantity) {
        if (quantity > reservedQuantity) {
            throw new BusinessRuleException("INVALID_CONFIRMATION",
                    "Cannot confirm more than reserved: " + reservedQuantity);
        }
        this.reservedQuantity -= quantity;
        registerEvent(new StockUpdatedEvent(getId(), sku, availableQuantity, reservedQuantity));
    }

    /** Release reservation — move reserved back to available (order cancelled). */
    public void releaseReservation(int quantity, UUID orderId) {
        if (quantity > reservedQuantity) {
            throw new BusinessRuleException("INVALID_RELEASE",
                    "Cannot release more than reserved: " + reservedQuantity);
        }
        this.reservedQuantity -= quantity;
        this.availableQuantity += quantity;
        registerEvent(new StockReleasedEvent(getId(), sku, quantity, orderId));
    }

    public void updateReorderThreshold(int threshold) {
        if (threshold < 0) throw new BusinessRuleException("INVALID_THRESHOLD", "Reorder threshold cannot be negative");
        this.reorderThreshold = threshold;
    }

    public int getTotalQuantity() {
        return availableQuantity + reservedQuantity;
    }

    public boolean isBelowReorderThreshold() {
        return availableQuantity <= reorderThreshold;
    }
}
