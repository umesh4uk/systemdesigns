package com.ecommerce.inventory.domain.repository;

import com.ecommerce.inventory.domain.model.InventoryItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {
    InventoryItem save(InventoryItem item);
    Optional<InventoryItem> findById(UUID id);
    Optional<InventoryItem> findBySkuAndWarehouse(String sku, String warehouseId);
    List<InventoryItem> findBySku(String sku);
    boolean existsBySku(String sku);
}
