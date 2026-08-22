package com.ecommerce.payment.domain.model;

import com.ecommerce.shared.domain.valueobject.Money;

import java.util.UUID;

/**
 * Value object for a refund operation.
 *
 * <p>Partial refunds are supported — {@code amount} may be less than the original
 * payment amount. The provider will validate that it does not exceed the captured amount.
 */
public record RefundRequest(
        UUID   paymentId,
        String providerTransactionId,   // opaque provider reference from the original capture
        Money  amount,
        String reason                   // visible to the customer on their statement
) {}
