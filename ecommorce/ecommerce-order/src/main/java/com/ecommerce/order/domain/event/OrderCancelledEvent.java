package com.ecommerce.order.domain.event;

import com.ecommerce.shared.domain.event.BaseDomainEvent;
import java.util.UUID;

public final class OrderCancelledEvent extends BaseDomainEvent {
    private final UUID orderId; private final String orderNumber;
    private final UUID customerId; private final String reason;
    public OrderCancelledEvent(UUID orderId, String orderNumber, UUID customerId, String reason) {
        super(); this.orderId = orderId; this.orderNumber = orderNumber;
        this.customerId = customerId; this.reason = reason;
    }
    @Override public String eventType() { return "order.cancelled"; }
    public UUID getOrderId()       { return orderId; }
    public String getOrderNumber() { return orderNumber; }
    public UUID getCustomerId()    { return customerId; }
    public String getReason()      { return reason; }
}
