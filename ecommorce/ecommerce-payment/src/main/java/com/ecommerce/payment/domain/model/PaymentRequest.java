package com.ecommerce.payment.domain.model;

import com.ecommerce.shared.domain.valueobject.Money;

import java.util.UUID;

/**
 * Value object carrying everything a {@link com.ecommerce.payment.infrastructure.provider.PaymentProvider}
 * needs to authorise or capture a payment.
 *
 * <p>Design decisions:
 * <ul>
 *   <li>{@code paymentMethodToken} is a provider-opaque token (e.g. Stripe's {@code pm_xxx}).
 *       Raw card details are NEVER stored or passed through the platform — the frontend
 *       tokenises them directly with the provider SDK before calling our API.</li>
 *   <li>{@code idempotencyKey} is mandatory. Providers honour it to prevent double-charges
 *       on network retries.</li>
 *   <li>{@code customerEmail} is included for soft-descriptor and receipt purposes.</li>
 * </ul>
 */
public record PaymentRequest(
        UUID    orderId,
        UUID    customerId,
        String  customerEmail,
        Money   amount,
        String  paymentMethodToken,     // provider token — not a raw card number
        String  idempotencyKey,
        String  description
) {
    public static PaymentRequest of(UUID orderId, UUID customerId, String customerEmail,
                                    Money amount, String paymentMethodToken,
                                    String idempotencyKey) {
        return new PaymentRequest(orderId, customerId, customerEmail, amount,
                paymentMethodToken, idempotencyKey,
                "Order " + orderId);
    }
}
