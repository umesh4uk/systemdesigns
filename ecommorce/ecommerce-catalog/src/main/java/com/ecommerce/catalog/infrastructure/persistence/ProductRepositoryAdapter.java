package com.ecommerce.catalog.infrastructure.persistence;

import com.ecommerce.catalog.domain.model.Product;
import com.ecommerce.catalog.domain.repository.ProductRepository;
import com.ecommerce.catalog.domain.repository.ProductSearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository jpa;

    @Override
    public Product save(Product product) {
        return jpa.save(product);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpa.findByIdWithDetails(id);
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return jpa.findBySku(sku.toUpperCase());
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpa.existsBySku(sku.toUpperCase());
    }

    @Override
    public Page<Product> findAll(ProductSearchCriteria criteria, Pageable pageable) {
        return jpa.findAll(ProductSpecifications.from(criteria), pageable);
    }

    @Override
    public void delete(Product product) {
        jpa.save(product); // soft delete — status = DELETED
    }
}
