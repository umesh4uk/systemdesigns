package com.ecommerce.shared.domain.event.payment;

import com.ecommerce.shared.domain.event.BaseDomainEvent;
import com.ecommerce.shared.domain.valueobject.Money;

import java.util.UUID;

/**
 * Integration event: published by the Payment bounded context when a refund is processed.
 * Consumed by Notification to inform the customer of the refund.
 */
public final class RefundProcessedEvent extends BaseDomainEvent {

    private final UUID paymentId;
    private final UUID orderId;
    private final Money amount;

    public RefundProcessedEvent(UUID paymentId, UUID orderId, Money amount) {
        super();
        this.paymentId = paymentId;
        this.orderId   = orderId;
        this.amount    = amount;
    }

    @Override
    public String eventType() { return "payment.refunded"; }

    public UUID getPaymentId() { return paymentId; }
    public UUID getOrderId()   { return orderId; }
    public Money getAmount()   { return amount; }
}
