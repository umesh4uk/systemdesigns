package com.ecommerce.inventory.application.service;

import com.ecommerce.inventory.domain.model.InventoryItem;
import com.ecommerce.inventory.domain.repository.InventoryRepository;
import com.ecommerce.shared.exception.ConflictException;
import com.ecommerce.shared.exception.InsufficientStockException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final String DEFAULT_WAREHOUSE = "WH-DEFAULT";

    private final InventoryRepository inventoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public InventoryItem createInventory(UUID productId, String sku, String warehouseId,
                                          int initialQty, int reorderThreshold) {
        String warehouse = warehouseId != null ? warehouseId : DEFAULT_WAREHOUSE;
        if (inventoryRepository.findBySkuAndWarehouse(sku, warehouse).isPresent()) {
            throw new ConflictException("Inventory already exists for SKU=" + sku + " warehouse=" + warehouse);
        }
        InventoryItem item = InventoryItem.create(productId, sku, warehouse, initialQty, reorderThreshold);
        return inventoryRepository.save(item);
    }

    @Transactional
    public InventoryItem addStock(String sku, String warehouseId, int quantity) {
        String warehouse = warehouseId != null ? warehouseId : DEFAULT_WAREHOUSE;
        InventoryItem item = inventoryRepository.findBySkuAndWarehouse(sku, warehouse)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", "SKU=" + sku));
        item.addStock(quantity);
        InventoryItem saved = inventoryRepository.save(item);
        publishEvents(saved);
        return saved;
    }

    @Transactional
    public void reserveStock(String sku, int quantity, UUID orderId) {
        List<InventoryItem> items = inventoryRepository.findBySku(sku);
        int remaining = quantity;
        for (InventoryItem item : items) {
            if (remaining <= 0) break;
            int toReserve = Math.min(remaining, item.getAvailableQuantity());
            if (toReserve > 0) {
                item.reserve(toReserve, orderId);
                inventoryRepository.save(item);
                publishEvents(item);
                remaining -= toReserve;
            }
        }
        if (remaining > 0) {
            throw new InsufficientStockException(sku, quantity, quantity - remaining);
        }
    }

    @Transactional
    public void releaseReservation(String sku, int quantity, UUID orderId) {
        List<InventoryItem> items = inventoryRepository.findBySku(sku);
        int remaining = quantity;
        for (InventoryItem item : items) {
            if (remaining <= 0) break;
            int toRelease = Math.min(remaining, item.getReservedQuantity());
            if (toRelease > 0) {
                item.releaseReservation(toRelease, orderId);
                inventoryRepository.save(item);
                publishEvents(item);
                remaining -= toRelease;
            }
        }
    }

    @Transactional
    public void confirmReservation(String sku, int quantity) {
        List<InventoryItem> items = inventoryRepository.findBySku(sku);
        int remaining = quantity;
        for (InventoryItem item : items) {
            if (remaining <= 0) break;
            int toConfirm = Math.min(remaining, item.getReservedQuantity());
            if (toConfirm > 0) {
                item.confirmReservation(toConfirm);
                inventoryRepository.save(item);
                publishEvents(item);
                remaining -= toConfirm;
            }
        }
    }

    @Transactional(readOnly = true)
    public int getAvailableQuantity(String sku) {
        return inventoryRepository.findBySku(sku).stream()
                .mapToInt(InventoryItem::getAvailableQuantity)
                .sum();
    }

    private void publishEvents(InventoryItem item) {
        item.getDomainEvents().forEach(eventPublisher::publishEvent);
        item.clearDomainEvents();
    }
}
