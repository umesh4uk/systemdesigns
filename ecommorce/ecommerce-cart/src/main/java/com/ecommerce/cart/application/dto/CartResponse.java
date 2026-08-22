package com.ecommerce.cart.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID cartId,
        UUID customerId,
        List<CartItemResponse> items,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal total,
        String currency,
        String appliedCouponCode
) {
    public record CartItemResponse(
            UUID productId,
            String sku,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal,
            String imageUrl
    ) {}
}
