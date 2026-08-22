package com.ecommerce.payment.domain.model;

import com.ecommerce.shared.domain.event.payment.PaymentCompletedEvent;
import com.ecommerce.shared.domain.event.payment.PaymentFailedEvent;
import com.ecommerce.shared.domain.event.payment.RefundProcessedEvent;
import com.ecommerce.shared.domain.model.AggregateRoot;
import com.ecommerce.shared.domain.valueobject.Money;
import com.ecommerce.shared.exception.BusinessRuleException;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payment aggregate root.
 * Idempotent: idempotency key prevents double-charging.
 */
@Getter
@Entity
@Table(name = "payments",
        uniqueConstraints = @UniqueConstraint(name = "uq_payment_idempotency",
                columnNames = "idempotency_key"))
public class Payment extends AggregateRoot {

    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 25)
    private PaymentStatus status;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    /** Authorization hold ID returned by provider.authorize() — before capture. */
    @Column(name = "authorization_id", length = 255)
    private String authorizationId;

    /** Final transaction ID after capture or single-step charge. */
    @Column(name = "provider_transaction_id", length = 255)
    private String providerTransactionId;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "refunded_amount", precision = 19, scale = 4)
    private BigDecimal refundedAmount;

    protected Payment() {}

    public static Payment initiate(UUID orderId, UUID customerId, Money amount,
                                    String provider, String idempotencyKey) {
        Payment p = new Payment();
        p.orderId         = orderId;
        p.customerId      = customerId;
        p.amount          = amount.getAmount();
        p.currency        = amount.getCurrencyCode();
        p.provider        = provider;
        p.idempotencyKey  = idempotencyKey;
        p.status          = PaymentStatus.PENDING;
        p.refundedAmount  = BigDecimal.ZERO;
        return p;
    }

    public void markProcessing() {
        if (status != PaymentStatus.PENDING) {
            throw new BusinessRuleException("INVALID_PAYMENT_STATE",
                    "Payment must be PENDING to start processing");
        }
        this.status = PaymentStatus.PROCESSING;
    }

    /** Record that funds have been authorized (reserved) but not yet captured. */
    public void authorize(String authId) {
        if (status != PaymentStatus.PENDING && status != PaymentStatus.PROCESSING) {
            throw new BusinessRuleException("INVALID_PAYMENT_STATE",
                    "Cannot authorize payment in state: " + status);
        }
        this.authorizationId = authId;
        this.status = PaymentStatus.PROCESSING;
    }

    /** Capture a previously authorized hold — funds are now charged. */
    public void capture(String captureTransactionId) {
        if (status != PaymentStatus.PROCESSING) {
            throw new BusinessRuleException("INVALID_PAYMENT_STATE",
                    "Payment must be in PROCESSING (authorized) state to capture");
        }
        this.status = PaymentStatus.COMPLETED;
        this.providerTransactionId = captureTransactionId;
        registerEvent(new PaymentCompletedEvent(getId(), orderId, customerId,
                Money.of(amount, currency)));
    }

    public void complete(String providerTransactionId) {
        if (status != PaymentStatus.PROCESSING && status != PaymentStatus.PENDING) {
            throw new BusinessRuleException("INVALID_PAYMENT_STATE",
                    "Cannot complete payment in state: " + status);
        }
        this.status = PaymentStatus.COMPLETED;
        this.providerTransactionId = providerTransactionId;
        registerEvent(new PaymentCompletedEvent(getId(), orderId, customerId,
                Money.of(amount, currency)));
    }

    public void fail(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        registerEvent(new PaymentFailedEvent(getId(), orderId, customerId, reason));
    }

    public void refund(BigDecimal refundAmt) {
        if (status != PaymentStatus.COMPLETED && status != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new BusinessRuleException("INVALID_REFUND_STATE",
                    "Can only refund COMPLETED or PARTIALLY_REFUNDED payments");
        }
        BigDecimal totalRefunded = this.refundedAmount.add(refundAmt);
        if (totalRefunded.compareTo(this.amount) > 0) {
            throw new BusinessRuleException("REFUND_EXCEEDS_AMOUNT",
                    "Total refund would exceed payment amount");
        }
        this.refundedAmount = totalRefunded;
        this.status = totalRefunded.compareTo(this.amount) == 0
                ? PaymentStatus.REFUNDED
                : PaymentStatus.PARTIALLY_REFUNDED;
        registerEvent(new RefundProcessedEvent(getId(), orderId, Money.of(refundAmt, currency)));
    }

    public Money getAmountMoney() { return Money.of(amount, currency); }
}
