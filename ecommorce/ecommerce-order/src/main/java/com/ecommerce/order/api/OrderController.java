package com.ecommerce.order.api;

import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.dto.PlaceOrderRequest;
import com.ecommerce.order.application.service.OrderService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Orders", description = "Order management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Place a new order from the current cart")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<OrderResponse> placeOrder(
            @AuthenticationPrincipal String customerId,
            @Valid @RequestBody PlaceOrderRequest request) {
        return ApiResponse.success(orderService.placeOrder(UUID.fromString(customerId), request));
    }

    @Operation(summary = "Get order by ID")
    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrder(
            @AuthenticationPrincipal String customerId,
            @PathVariable UUID orderId) {
        return ApiResponse.success(orderService.getOrder(orderId, UUID.fromString(customerId)));
    }

    @Operation(summary = "Get order history")
    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> getOrders(
            @AuthenticationPrincipal String customerId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.success(
                orderService.getCustomerOrders(UUID.fromString(customerId), pageable));
    }

    @Operation(summary = "Cancel an order")
    @PostMapping("/{orderId}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(
            @AuthenticationPrincipal String customerId,
            @PathVariable UUID orderId,
            @RequestParam(required = false) String reason) {
        return ApiResponse.success(
                orderService.cancelOrder(orderId, UUID.fromString(customerId), reason));
    }

    @Operation(summary = "Request a return")
    @PostMapping("/{orderId}/return")
    public ApiResponse<OrderResponse> requestReturn(
            @AuthenticationPrincipal String customerId,
            @PathVariable UUID orderId) {
        return ApiResponse.success(orderService.requestReturn(orderId, UUID.fromString(customerId)));
    }
}
