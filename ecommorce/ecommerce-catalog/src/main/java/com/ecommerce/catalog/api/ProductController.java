package com.ecommerce.catalog.api;

import com.ecommerce.catalog.application.dto.ProductDetailResponse;
import com.ecommerce.catalog.application.dto.ProductSummaryResponse;
import com.ecommerce.catalog.application.service.ProductService;
import com.ecommerce.shared.api.response.ApiResponse;
import com.ecommerce.shared.api.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Public (read-only) product catalog endpoints.
 */
@Tag(name = "Products", description = "Browse and search products")
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Search and filter products")
    @GetMapping
    public ApiResponse<PageResponse<ProductSummaryResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "20")  int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc")  String sortDir) {

        Sort.Direction dir = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(dir, sortBy));
        return ApiResponse.success(
                productService.searchProducts(keyword, categoryId, brand, minPrice, maxPrice, pageable));
    }

    @Operation(summary = "Get product detail by ID")
    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> getProduct(@PathVariable UUID id) {
        return ApiResponse.success(productService.getProduct(id));
    }

    @Operation(summary = "Get product detail by SKU")
    @GetMapping("/sku/{sku}")
    public ApiResponse<ProductDetailResponse> getProductBySku(@PathVariable String sku) {
        return ApiResponse.success(productService.getProductBySku(sku));
    }
}
