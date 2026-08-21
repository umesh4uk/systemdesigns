package com.ecommerce.catalog.domain.repository;

import com.ecommerce.catalog.domain.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(UUID id);

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    /** Fetch all root categories (no parent) with children eagerly. */
    List<Category> findAllRoots();

    /** Fetch all active categories for a given parent. */
    List<Category> findByParentId(UUID parentId);
}
