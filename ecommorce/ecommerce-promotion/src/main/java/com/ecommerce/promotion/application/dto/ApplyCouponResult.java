package com.ecommerce.promotion.application.dto;

import com.ecommerce.shared.domain.valueobject.Money;

import java.util.UUID;

public record ApplyCouponResult(
        UUID couponId,
        String code,
        Money discountAmount,
        Money finalTotal
) {}
