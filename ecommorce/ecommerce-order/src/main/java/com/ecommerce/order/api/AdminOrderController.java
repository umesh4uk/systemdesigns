package com.ecommerce.order.api;

import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.service.OrderService;
import com.ecommerce.order.domain.model.OrderStatus;
import com.ecommerce.shared.api.response.ApiResponse;
import com.ecommerce.shared.api.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Admin - Orders")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    @Operation(summary = "Get orders by status")
    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> getByStatus(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (status != null) {
            return ApiResponse.success(
                    PageResponse.from(orderService.getOrdersByStatus(status, pageable)));
        }
        return ApiResponse.success(orderService.getAllOrders(pageable));
    }

    @Operation(summary = "Update order status")
    @PatchMapping("/{orderId}/status")
    public ApiResponse<OrderResponse> updateStatus(
            @PathVariable UUID orderId,
            @RequestParam OrderStatus status,
            @RequestParam(required = false) String trackingNumber) {
        return ApiResponse.success(orderService.updateStatus(orderId, status, trackingNumber));
    }
}
