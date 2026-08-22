package com.ecommerce.payment.domain.model;

/**
 * Outcome of a {@link com.ecommerce.payment.infrastructure.provider.PaymentProvider} operation.
 *
 * <p>Using a result object instead of exceptions for expected failure outcomes
 * (insufficient funds, card declined) keeps the provider interface clean and
 * makes the caller's control flow explicit.
 */
public record PaymentResult(
        boolean success,
        String  providerTransactionId,  // non-null on success
        String  failureCode,            // provider-specific code, non-null on failure
        String  failureMessage          // human-readable, never exposed to end users
) {
    public static PaymentResult success(String transactionId) {
        return new PaymentResult(true, transactionId, null, null);
    }

    public static PaymentResult failure(String code, String message) {
        return new PaymentResult(false, null, code, message);
    }
}
