package com.ecommerce.catalog.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CategoryRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Pattern(regexp = "[a-z0-9\\-]+") @Size(max = 150) String slug,
        @Size(max = 1000) String description,
        @Size(max = 1024) String imageUrl,
        UUID parentId,
        int displayOrder
) {}
