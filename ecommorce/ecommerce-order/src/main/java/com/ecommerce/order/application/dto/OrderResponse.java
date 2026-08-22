package com.ecommerce.order.application.dto;

import com.ecommerce.order.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        UUID customerId,
        OrderStatus status,
        List<OrderItemResponse> items,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal shipping,
        BigDecimal total,
        String currency,
        String couponCode,
        String trackingNumber,
        String cancellationReason,
        Instant createdAt
) {
    public record OrderItemResponse(
            UUID productId, String sku, String productName,
            int quantity, BigDecimal unitPrice, BigDecimal lineTotal, String currency
    ) {}
}
