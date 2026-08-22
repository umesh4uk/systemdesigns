package com.ecommerce.payment.application.dto;

import com.ecommerce.payment.domain.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id, UUID orderId, UUID customerId,
        PaymentStatus status, BigDecimal amount, String currency,
        String provider, String providerTransactionId,
        String failureReason, BigDecimal refundedAmount, Instant createdAt
) {}
