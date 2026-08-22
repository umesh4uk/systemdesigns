package com.ecommerce.inventory.infrastructure.persistence;

import com.ecommerce.inventory.domain.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaInventoryRepository extends JpaRepository<InventoryItem, UUID> {
    Optional<InventoryItem> findBySkuAndWarehouseId(String sku, String warehouseId);
    List<InventoryItem> findBySku(String sku);
    boolean existsBySku(String sku);

    // ── Dashboard queries ─────────────────────────────────────────────────────
    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.availableQuantity <= i.reorderThreshold")
    long countByAvailableQuantityLessThanEqualReorderThreshold();
}
