package com.ecommerce.catalog.application.mapper;

import com.ecommerce.catalog.application.dto.CategoryResponse;
import com.ecommerce.catalog.domain.model.Category;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category c) {
        List<CategoryResponse> children = c.getChildren().stream()
                .map(this::toResponse)
                .toList();

        return new CategoryResponse(
                c.getId(), c.getName(), c.getSlug(), c.getDescription(),
                c.getImageUrl(),
                c.getParent() != null ? c.getParent().getId() : null,
                c.getDisplayOrder(), c.isActive(), children);
    }
}
