package com.ecommerce.catalog.infrastructure.persistence;

import com.ecommerce.catalog.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.parent IS NULL ORDER BY c.displayOrder")
    List<Category> findAllRoots();

    List<Category> findByParentIdOrderByDisplayOrder(UUID parentId);
}
