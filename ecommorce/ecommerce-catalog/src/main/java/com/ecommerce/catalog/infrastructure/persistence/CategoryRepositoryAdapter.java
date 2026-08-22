package com.ecommerce.catalog.infrastructure.persistence;

import com.ecommerce.catalog.domain.model.Category;
import com.ecommerce.catalog.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepository {

    private final JpaCategoryRepository jpa;

    @Override public Category save(Category category)           { return jpa.save(category); }
    @Override public Optional<Category> findById(UUID id)       { return jpa.findById(id); }
    @Override public Optional<Category> findBySlug(String slug) { return jpa.findBySlug(slug); }
    @Override public boolean existsBySlug(String slug)          { return jpa.existsBySlug(slug); }
    @Override public List<Category> findAllRoots()              { return jpa.findAllRoots(); }

    @Override
    public List<Category> findByParentId(UUID parentId) {
        return jpa.findByParentIdOrderByDisplayOrder(parentId);
    }
}
