package com.ecommerce.cart.api;

import com.ecommerce.cart.application.dto.AddCartItemRequest;
import com.ecommerce.cart.application.dto.CartResponse;
import com.ecommerce.cart.application.service.CartService;
import com.ecommerce.shared.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Cart", description = "Shopping cart management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get current cart")
    @GetMapping
    public ApiResponse<CartResponse> getCart(@AuthenticationPrincipal String customerId) {
        return ApiResponse.success(cartService.getCart(UUID.fromString(customerId)));
    }

    @Operation(summary = "Add item to cart")
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CartResponse> addItem(
            @AuthenticationPrincipal String customerId,
            @Valid @RequestBody AddCartItemRequest request) {
        return ApiResponse.success(cartService.addItem(UUID.fromString(customerId), request));
    }

    @Operation(summary = "Update item quantity")
    @PutMapping("/items/{productId}")
    public ApiResponse<CartResponse> updateQuantity(
            @AuthenticationPrincipal String customerId,
            @PathVariable UUID productId,
            @RequestParam int quantity) {
        return ApiResponse.success(
                cartService.updateItemQuantity(UUID.fromString(customerId), productId, quantity));
    }

    @Operation(summary = "Remove item from cart")
    @DeleteMapping("/items/{productId}")
    public ApiResponse<CartResponse> removeItem(
            @AuthenticationPrincipal String customerId,
            @PathVariable UUID productId) {
        return ApiResponse.success(cartService.removeItem(UUID.fromString(customerId), productId));
    }

    @Operation(summary = "Apply coupon to cart")
    @PostMapping("/coupon")
    public ApiResponse<CartResponse> applyCoupon(
            @AuthenticationPrincipal String customerId,
            @RequestParam String code) {
        return ApiResponse.success(cartService.applyCoupon(UUID.fromString(customerId), code));
    }

    @Operation(summary = "Remove coupon from cart")
    @DeleteMapping("/coupon")
    public ApiResponse<CartResponse> removeCoupon(@AuthenticationPrincipal String customerId) {
        return ApiResponse.success(cartService.removeCoupon(UUID.fromString(customerId)));
    }

    @Operation(summary = "Clear entire cart")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@AuthenticationPrincipal String customerId) {
        cartService.clearCart(UUID.fromString(customerId));
    }
}
