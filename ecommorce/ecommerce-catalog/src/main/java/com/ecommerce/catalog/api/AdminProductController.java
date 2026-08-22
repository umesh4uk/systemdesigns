package com.ecommerce.catalog.api;

import com.ecommerce.catalog.application.dto.*;
import com.ecommerce.catalog.application.service.ProductService;
import com.ecommerce.catalog.domain.model.ProductStatus;
import com.ecommerce.shared.api.response.ApiResponse;
import com.ecommerce.shared.api.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin product management endpoints.
 */
@Tag(name = "Admin - Products", description = "Admin product catalog management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductService productService;

    @Operation(summary = "Create a new product (DRAFT)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductDetailResponse> create(@Valid @RequestBody CreateProductRequest request) {
        return ApiResponse.success(productService.createProduct(request));
    }

    @Operation(summary = "Update product details")
    @PutMapping("/{id}")
    public ApiResponse<ProductDetailResponse> update(@PathVariable UUID id,
                                                     @Valid @RequestBody UpdateProductRequest request) {
        return ApiResponse.success(productService.updateProduct(id, request));
    }

    @Operation(summary = "Publish a product (DRAFT → ACTIVE)")
    @PostMapping("/{id}/publish")
    public ApiResponse<ProductDetailResponse> publish(@PathVariable UUID id) {
        return ApiResponse.success(productService.publishProduct(id));
    }

    @Operation(summary = "Archive a product (ACTIVE → ARCHIVED)")
    @PostMapping("/{id}/archive")
    public ApiResponse<ProductDetailResponse> archive(@PathVariable UUID id) {
        return ApiResponse.success(productService.archiveProduct(id));
    }

    @Operation(summary = "Soft-delete a product")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        productService.deleteProduct(id);
    }

    @Operation(summary = "Add image to product")
    @PostMapping("/{id}/images")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductDetailResponse> addImage(
            @PathVariable UUID id,
            @RequestParam String url,
            @RequestParam(required = false) String altText,
            @RequestParam(defaultValue = "0") int displayOrder) {
        return ApiResponse.success(productService.addImage(id, url, altText, displayOrder));
    }

    @Operation(summary = "Set/update a product attribute")
    @PutMapping("/{id}/attributes/{key}")
    public ApiResponse<ProductDetailResponse> setAttribute(
            @PathVariable UUID id,
            @PathVariable String key,
            @RequestParam String value) {
        return ApiResponse.success(productService.setAttribute(id, key, value));
    }

    @Operation(summary = "Admin product search (all statuses)")
    @GetMapping
    public ApiResponse<PageResponse<ProductSummaryResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        var pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.success(
                productService.adminSearchProducts(keyword, categoryId, brand, status, pageable));
    }
}
