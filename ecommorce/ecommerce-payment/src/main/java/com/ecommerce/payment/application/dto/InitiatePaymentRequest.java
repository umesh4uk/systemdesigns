package com.ecommerce.payment.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record InitiatePaymentRequest(
        @NotNull UUID orderId,
        @NotBlank String paymentMethodToken,
        @NotBlank String idempotencyKey
) {}
