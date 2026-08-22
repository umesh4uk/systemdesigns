package com.ecommerce.payment.infrastructure.provider;

import com.ecommerce.payment.domain.model.PaymentRequest;
import com.ecommerce.payment.domain.model.PaymentResult;
import com.ecommerce.payment.domain.model.RefundRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mock payment provider for development and testing.
 *
 * <p>Simulates the authorize → capture → refund lifecycle without calling
 * any real payment gateway. Intended for:
 * <ul>
 *   <li>Local development (active by default via {@code @Primary}).</li>
 *   <li>Integration tests that do not want to depend on Stripe/PayPal sandboxes.</li>
 * </ul>
 *
 * <p>To simulate a failure, pass a payment method token prefixed with {@code "fail_"}.
 * To simulate a timeout, use {@code "timeout_"}. Everything else succeeds.
 *
 * <p>Replace with a real provider implementation and bind via
 * {@code @ConditionalOnProperty(name = "app.payment.provider", havingValue = "stripe")}
 * in production.
 */
@Slf4j
@Component
@Primary   // overridden when a real provider bean is present
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public String providerName() { return "MOCK"; }

    @Override
    public PaymentResult authorize(PaymentRequest request) {
        log.info("[MOCK] Authorizing {} {} for order={} idempotencyKey={}",
                request.amount().getAmount(), request.amount().getCurrencyCode(),
                request.orderId(), request.idempotencyKey());

        if (shouldFail(request.paymentMethodToken())) {
            return PaymentResult.failure("CARD_DECLINED",
                    "Mock: card declined for token " + request.paymentMethodToken());
        }
        if (shouldTimeout(request.paymentMethodToken())) {
            return PaymentResult.failure("TIMEOUT",
                    "Mock: provider timed out");
        }

        // Return a mock authorization hold ID
        String authId = "MOCK-AUTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("[MOCK] Authorization granted: authId={}", authId);
        return PaymentResult.success(authId);
    }

    @Override
    public PaymentResult capture(String authorizationId, PaymentRequest request) {
        log.info("[MOCK] Capturing authId={} for order={}",
                authorizationId, request.orderId());

        if (shouldFail(request.paymentMethodToken())) {
            return PaymentResult.failure("CAPTURE_FAILED",
                    "Mock: capture failed for authId " + authorizationId);
        }

        String captureId = "MOCK-CAP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("[MOCK] Capture completed: captureId={}", captureId);
        return PaymentResult.success(captureId);
    }

    @Override
    public PaymentResult refund(RefundRequest request) {
        log.info("[MOCK] Refunding {} {} for payment={}",
                request.amount().getAmount(), request.amount().getCurrencyCode(),
                request.paymentId());

        String refundId = "MOCK-REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("[MOCK] Refund processed: refundId={}", refundId);
        return PaymentResult.success(refundId);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private boolean shouldFail(String token) {
        return token != null && token.startsWith("fail_");
    }

    private boolean shouldTimeout(String token) {
        return token != null && token.startsWith("timeout_");
    }
}
