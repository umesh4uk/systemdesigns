package com.ecommerce.catalog.application.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequest(
        @NotBlank @Size(min = 3, max = 64) String sku,
        @NotBlank @Size(max = 300) String name,
        @Size(max = 5000) String description,
        @Size(max = 500) String shortDescription,
        UUID categoryId,
        @Size(max = 150) String brand,
        @NotNull @DecimalMin("0.00") BigDecimal basePrice,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @Min(0) Integer weightGrams
) {}
