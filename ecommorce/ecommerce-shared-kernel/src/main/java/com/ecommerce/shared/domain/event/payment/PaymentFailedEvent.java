package com.ecommerce.shared.domain.event.payment;

import com.ecommerce.shared.domain.event.BaseDomainEvent;

import java.util.UUID;

/**
 * Integration event: published by the Payment bounded context when a payment fails.
 * Consumed by the Order bounded context to mark the order PAYMENT_FAILED,
 * and by Notification to alert the customer.
 */
public final class PaymentFailedEvent extends BaseDomainEvent {

    private final UUID paymentId;
    private final UUID orderId;
    private final UUID customerId;
    private final String reason;

    public PaymentFailedEvent(UUID paymentId, UUID orderId, UUID customerId, String reason) {
        super();
        this.paymentId  = paymentId;
        this.orderId    = orderId;
        this.customerId = customerId;
        this.reason     = reason;
    }

    @Override
    public String eventType() { return "payment.failed"; }

    public UUID getPaymentId()  { return paymentId; }
    public UUID getOrderId()    { return orderId; }
    public UUID getCustomerId() { return customerId; }
    public String getReason()   { return reason; }
}
