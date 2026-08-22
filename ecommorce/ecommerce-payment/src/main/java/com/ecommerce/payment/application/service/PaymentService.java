package com.ecommerce.payment.application.service;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.repository.OrderRepository;
import com.ecommerce.payment.application.dto.InitiatePaymentRequest;
import com.ecommerce.payment.application.dto.PaymentResponse;
import com.ecommerce.payment.domain.model.Payment;
import com.ecommerce.payment.domain.model.PaymentRequest;
import com.ecommerce.payment.domain.model.PaymentResult;
import com.ecommerce.payment.domain.model.RefundRequest;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import com.ecommerce.payment.infrastructure.provider.PaymentProvider;
import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payment application service — orchestrates the authorize → capture → refund lifecycle.
 *
 * <p><b>Two-phase payment flow:</b>
 * <ol>
 *   <li>{@link #initiatePayment} calls {@code provider.authorize()} to reserve funds.</li>
 *   <li>{@link #capturePayment} calls {@code provider.capture()} after order confirmation.</li>
 *   <li>On failure at any stage, inventory is released via the event-driven compensation chain.</li>
 * </ol>
 *
 * <p><b>Idempotency:</b> if a payment with the same {@code idempotencyKey} already exists,
 * the previous result is returned without calling the provider again.
 *
 * <p><b>Security:</b>
 * <ul>
 *   <li>Raw card data never passes through this service — only provider tokens.</li>
 *   <li>Failure reasons are logged internally but never returned to the API caller
 *       to avoid leaking provider-specific error codes that could aid card testing.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository     paymentRepository;
    private final OrderRepository       orderRepository;
    private final PaymentProvider       paymentProvider;
    private final ApplicationEventPublisher eventPublisher;

    // ── authorize ─────────────────────────────────────────────────────────────

    /**
     * Step 1 — Authorize (reserve) funds without capturing.
     * The order is transitioned to PAYMENT_PENDING before calling the provider.
     */
    @Transactional
    public PaymentResponse initiatePayment(UUID customerId,
                                           InitiatePaymentRequest request) {
        // ── idempotency guard ────────────────────────────────────────────────
        var existing = paymentRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            log.info("Idempotent payment request [key={}]", request.idempotencyKey());
            return toResponse(existing.get());
        }

        Order order = loadAndOwn(request.orderId(), customerId);
        Money amount = order.getTotalMoney();

        Payment payment = Payment.initiate(order.getId(), customerId,
                amount, paymentProvider.providerName(), request.idempotencyKey());
        payment.markProcessing();

        // Transition order to PAYMENT_PENDING so the state machine is correct
        order.markPaymentPending();
        orderRepository.save(order);

        // ── call provider (authorize only — no charge yet) ───────────────────
        PaymentRequest providerRequest = PaymentRequest.of(
                order.getId(), customerId, null,    // email lookup can be added
                amount, request.paymentMethodToken(), request.idempotencyKey());

        PaymentResult result = paymentProvider.authorize(providerRequest);

        if (result.success()) {
            payment.authorize(result.providerTransactionId());
            log.info("Payment authorized [orderId={}, authId={}]",
                    order.getId(), result.providerTransactionId());
        } else {
            // Sanitize — never expose raw provider failure codes to the caller
            log.warn("Payment authorization failed [orderId={}, code={}, msg={}]",
                    order.getId(), result.failureCode(), result.failureMessage());
            payment.fail(result.failureCode());     // internal only
        }

        Payment saved = paymentRepository.save(payment);
        publishEvents(saved);
        return toResponse(saved);
    }

    // ── capture ───────────────────────────────────────────────────────────────

    /**
     * Step 2 — Capture the authorization hold (triggered after order confirmation).
     * Called by the order {@code PaymentEventListener} or directly by an admin.
     */
    @Transactional
    public PaymentResponse capturePayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        if (payment.getAuthorizationId() == null) {
            throw new BusinessRuleException("NO_AUTHORIZATION",
                    "Cannot capture — payment has no authorization hold");
        }

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", payment.getOrderId()));

        PaymentRequest providerRequest = PaymentRequest.of(
                payment.getOrderId(), payment.getCustomerId(), null,
                payment.getAmountMoney(), "N/A",  // token not needed for capture
                payment.getIdempotencyKey() + "-capture");

        PaymentResult result = paymentProvider.capture(
                payment.getAuthorizationId(), providerRequest);

        if (result.success()) {
            payment.capture(result.providerTransactionId());
            log.info("Payment captured [orderId={}, captureId={}]",
                    payment.getOrderId(), result.providerTransactionId());
        } else {
            log.warn("Payment capture failed [orderId={}, code={}]",
                    payment.getOrderId(), result.failureCode());
            payment.fail(result.failureCode());
        }

        Payment saved = paymentRepository.save(payment);
        publishEvents(saved);
        return toResponse(saved);
    }

    // ── refund ────────────────────────────────────────────────────────────────

    @Transactional
    public PaymentResponse processRefund(UUID paymentId, BigDecimal amount, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        com.ecommerce.shared.domain.valueobject.Money refundMoney =
                com.ecommerce.shared.domain.valueobject.Money.of(amount, payment.getCurrency());

        RefundRequest refundRequest = new RefundRequest(
                paymentId, payment.getProviderTransactionId(),
                refundMoney, reason);

        PaymentResult result = paymentProvider.refund(refundRequest);
        if (!result.success()) {
            log.warn("Refund failed [paymentId={}, code={}]",
                    paymentId, result.failureCode());
            throw new BusinessRuleException("REFUND_FAILED",
                    "Refund could not be processed. Please try again.");
        }

        payment.refund(amount);
        Payment saved = paymentRepository.save(payment);
        publishEvents(saved);
        log.info("Refund processed [paymentId={}, refundId={}]",
                paymentId, result.providerTransactionId());
        return toResponse(saved);
    }

    // ── read ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId) {
        return toResponse(paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId)));
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrder(UUID orderId) {
        return toResponse(paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payment", "orderId:" + orderId)));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Order loadAndOwn(UUID orderId, UUID customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (!order.getCustomerId().equals(customerId)) {
            throw new ResourceNotFoundException("Order", orderId);
        }
        return order;
    }

    private void publishEvents(Payment payment) {
        payment.getDomainEvents().forEach(eventPublisher::publishEvent);
        payment.clearDomainEvents();
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
                p.getId(), p.getOrderId(), p.getCustomerId(),
                p.getStatus(), p.getAmount(), p.getCurrency(),
                p.getProvider(), p.getProviderTransactionId(),
                /* Never expose raw failure reason — return null */
                null,
                p.getRefundedAmount(), p.getCreatedAt());
    }
}
