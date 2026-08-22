package com.ecommerce.inventory.api;

import com.ecommerce.inventory.application.service.InventoryService;
import com.ecommerce.shared.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Inventory", description = "Stock management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Initialize inventory for a product SKU")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<String> createInventory(
            @RequestParam UUID productId,
            @RequestParam @NotBlank String sku,
            @RequestParam(required = false) String warehouseId,
            @RequestParam(defaultValue = "0") @Min(0) int initialQuantity,
            @RequestParam(defaultValue = "5") @Min(0) int reorderThreshold) {
        inventoryService.createInventory(productId, sku, warehouseId, initialQuantity, reorderThreshold);
        return ApiResponse.success("Inventory initialized");
    }

    @Operation(summary = "Add stock for a SKU")
    @PostMapping("/{sku}/stock")
    public ApiResponse<String> addStock(
            @PathVariable String sku,
            @RequestParam(required = false) String warehouseId,
            @RequestParam @Min(1) int quantity) {
        inventoryService.addStock(sku, warehouseId, quantity);
        return ApiResponse.success("Stock added");
    }

    @Operation(summary = "Get available quantity for a SKU")
    @GetMapping("/{sku}/available")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Integer> getAvailable(@PathVariable String sku) {
        return ApiResponse.success(inventoryService.getAvailableQuantity(sku));
    }
}
