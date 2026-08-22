package com.ecommerce.order.domain.event;

import com.ecommerce.shared.domain.event.BaseDomainEvent;
import java.util.UUID;

public final class OrderDeliveredEvent extends BaseDomainEvent {
    private final UUID orderId; private final String orderNumber; private final UUID customerId;
    public OrderDeliveredEvent(UUID orderId, String orderNumber, UUID customerId) {
        super(); this.orderId = orderId; this.orderNumber = orderNumber; this.customerId = customerId;
    }
    @Override public String eventType() { return "order.delivered"; }
    public UUID getOrderId()       { return orderId; }
    public String getOrderNumber() { return orderNumber; }
    public UUID getCustomerId()    { return customerId; }
}
