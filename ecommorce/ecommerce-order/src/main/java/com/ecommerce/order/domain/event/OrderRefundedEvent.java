package com.ecommerce.order.domain.event;

import com.ecommerce.shared.domain.event.BaseDomainEvent;
import com.ecommerce.shared.domain.valueobject.Money;
import java.util.UUID;

public final class OrderRefundedEvent extends BaseDomainEvent {
    private final UUID orderId; private final String orderNumber;
    private final UUID customerId; private final Money refundAmount;
    public OrderRefundedEvent(UUID orderId, String orderNumber, UUID customerId, Money refundAmount) {
        super(); this.orderId = orderId; this.orderNumber = orderNumber;
        this.customerId = customerId; this.refundAmount = refundAmount;
    }
    @Override public String eventType() { return "order.refunded"; }
    public UUID getOrderId()       { return orderId; }
    public String getOrderNumber() { return orderNumber; }
    public UUID getCustomerId()    { return customerId; }
    public Money getRefundAmount() { return refundAmount; }
}
