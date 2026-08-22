package com.ecommerce.catalog.application.dto;

import java.util.List;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        String description,
        String imageUrl,
        UUID parentId,
        int displayOrder,
        boolean active,
        List<CategoryResponse> children
) {}
