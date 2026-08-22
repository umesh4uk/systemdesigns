package com.ecommerce.order.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PlaceOrderRequest(
        @NotNull UUID shippingAddressId,
        @NotNull UUID billingAddressId,
        String couponCode
) {}
