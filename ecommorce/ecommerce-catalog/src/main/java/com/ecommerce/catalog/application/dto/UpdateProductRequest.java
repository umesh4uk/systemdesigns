package com.ecommerce.catalog.application.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductRequest(
        @NotBlank @Size(max = 300) String name,
        @Size(max = 5000) String description,
        @Size(max = 500) String shortDescription,
        UUID categoryId,
        @Size(max = 150) String brand,
        @DecimalMin("0.00") BigDecimal basePrice,
        String currency,
        @Min(0) Integer weightGrams
) {}
