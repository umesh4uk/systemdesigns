package com.ecommerce.shared.domain.event.payment;

import com.ecommerce.shared.domain.event.BaseDomainEvent;
import com.ecommerce.shared.domain.valueobject.Money;

import java.util.UUID;

/**
 * Integration event: published by the Payment bounded context when a payment
 * completes successfully. Consumed by the Order bounded context to confirm the
 * order, and by Notification to send a payment receipt.
 *
 * <p>Lives in shared-kernel so both producer (payment) and consumers (order,
 * notification) can compile against it without creating a circular dependency.
 */
public final class PaymentCompletedEvent extends BaseDomainEvent {

    private final UUID paymentId;
    private final UUID orderId;
    private final UUID customerId;
    private final Money amount;

    public PaymentCompletedEvent(UUID paymentId, UUID orderId, UUID customerId, Money amount) {
        super();
        this.paymentId  = paymentId;
        this.orderId    = orderId;
        this.customerId = customerId;
        this.amount     = amount;
    }

    @Override
    public String eventType() { return "payment.completed"; }

    public UUID getPaymentId()  { return paymentId; }
    public UUID getOrderId()    { return orderId; }
    public UUID getCustomerId() { return customerId; }
    public Money getAmount()    { return amount; }
}
