package com.ecommerce.catalog.application.service;

import com.ecommerce.catalog.application.dto.CategoryRequest;
import com.ecommerce.catalog.application.dto.CategoryResponse;
import com.ecommerce.catalog.application.mapper.CategoryMapper;
import com.ecommerce.catalog.domain.model.Category;
import com.ecommerce.catalog.domain.repository.CategoryRepository;
import com.ecommerce.shared.exception.ConflictException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {
        return categoryRepository.findAllRoots().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategory(UUID id) {
        return categoryMapper.toResponse(loadCategory(id));
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        return categoryMapper.toResponse(
                categoryRepository.findBySlug(slug)
                        .orElseThrow(() -> new ResourceNotFoundException("Category", "slug:" + slug)));
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new ConflictException("Category slug already exists: " + request.slug());
        }

        Category category;
        if (request.parentId() != null) {
            Category parent = loadCategory(request.parentId());
            category = Category.createChild(request.name(), request.slug(),
                    request.description(), parent, request.displayOrder());
        } else {
            category = Category.createRoot(request.name(), request.slug(),
                    request.description(), request.displayOrder());
        }

        if (request.imageUrl() != null) {
            category.update(category.getName(), category.getDescription(),
                    request.imageUrl(), category.getDisplayOrder());
        }

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        Category category = loadCategory(id);

        // If slug changed, check uniqueness
        if (!category.getSlug().equals(request.slug())
                && categoryRepository.existsBySlug(request.slug())) {
            throw new ConflictException("Category slug already exists: " + request.slug());
        }

        category.update(request.name(), request.description(),
                request.imageUrl(), request.displayOrder());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deactivateCategory(UUID id) {
        Category category = loadCategory(id);
        category.deactivate();
        categoryRepository.save(category);
    }

    private Category loadCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }
}
