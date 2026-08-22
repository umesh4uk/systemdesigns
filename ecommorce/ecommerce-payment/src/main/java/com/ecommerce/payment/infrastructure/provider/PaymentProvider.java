package com.ecommerce.payment.infrastructure.provider;

import com.ecommerce.payment.domain.model.PaymentRequest;
import com.ecommerce.payment.domain.model.PaymentResult;
import com.ecommerce.payment.domain.model.RefundRequest;

/**
 * Strategy interface for payment gateway integration.
 *
 * <p><b>Why Strategy pattern?</b> The Order domain must never be coupled to a
 * specific payment provider. By depending on this interface the platform can:
 * <ul>
 *   <li>Swap providers (Stripe → PayPal) by changing a single Spring bean.</li>
 *   <li>Support multiple providers simultaneously (e.g. different currencies).</li>
 *   <li>Test the payment flow without a real gateway (MockPaymentProvider).</li>
 * </ul>
 *
 * <p><b>Two-phase flow (authorize → capture):</b>
 * <ol>
 *   <li>{@link #authorize} — reserves funds on the card, returning an authorization hold.
 *       No money moves yet. This is called at order placement.</li>
 *   <li>{@link #capture} — converts the hold into an actual charge.
 *       Called after inventory is confirmed and the order is fully validated.</li>
 * </ol>
 * This prevents charging customers for orders that later fail (e.g. out-of-stock).
 *
 * <p><b>Idempotency:</b> providers honour the {@code idempotencyKey} on {@link PaymentRequest}
 * — retrying with the same key returns the previous result rather than creating a new charge.
 *
 * <p><b>PCI-DSS note:</b> raw card data is NEVER passed through this interface.
 * Only opaque provider tokens (e.g. Stripe's {@code pm_xxx}) are accepted.
 * Card tokenisation happens in the frontend via the provider's JS SDK.
 */
public interface PaymentProvider {

    /** Logical name for logging and idempotency-key namespacing. */
    String providerName();

    /**
     * Authorize (reserve) funds without capturing.
     *
     * @param request payment details including the tokenised payment method
     * @return success with an authorization hold ID, or a structured failure
     */
    PaymentResult authorize(PaymentRequest request);

    /**
     * Capture a previously authorized hold.
     *
     * @param authorizationId the {@code providerTransactionId} from a prior {@link #authorize}
     * @param request         the original payment details (amount must not exceed authorized amount)
     * @return success with a capture transaction ID, or a structured failure
     */
    PaymentResult capture(String authorizationId, PaymentRequest request);

    /**
     * Refund a previously captured transaction (full or partial).
     *
     * <p>IMPORTANT: do not retry refunds blindly. Use the {@code idempotencyKey}
     * on {@link PaymentRequest} to avoid double-refunds.
     *
     * @return success with a refund transaction ID, or a structured failure
     */
    PaymentResult refund(RefundRequest request);
}
