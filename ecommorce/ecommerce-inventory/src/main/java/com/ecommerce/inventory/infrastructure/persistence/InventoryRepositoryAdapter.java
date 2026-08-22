package com.ecommerce.inventory.infrastructure.persistence;

import com.ecommerce.inventory.domain.model.InventoryItem;
import com.ecommerce.inventory.domain.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class InventoryRepositoryAdapter implements InventoryRepository {
    private final JpaInventoryRepository jpa;

    @Override public InventoryItem save(InventoryItem item)                            { return jpa.save(item); }
    @Override public Optional<InventoryItem> findById(UUID id)                         { return jpa.findById(id); }
    @Override public List<InventoryItem> findBySku(String sku)                         { return jpa.findBySku(sku.toUpperCase()); }
    @Override public boolean existsBySku(String sku)                                   { return jpa.existsBySku(sku.toUpperCase()); }
    @Override public Optional<InventoryItem> findBySkuAndWarehouse(String sku, String w){ return jpa.findBySkuAndWarehouseId(sku.toUpperCase(), w); }
}
