package com.ecommerce.order.domain.event;

import com.ecommerce.shared.domain.event.BaseDomainEvent;
import java.util.UUID;

public final class OrderShippedEvent extends BaseDomainEvent {
    private final UUID orderId; private final String orderNumber;
    private final UUID customerId; private final String trackingNumber;
    public OrderShippedEvent(UUID orderId, String orderNumber, UUID customerId, String trackingNumber) {
        super(); this.orderId = orderId; this.orderNumber = orderNumber;
        this.customerId = customerId; this.trackingNumber = trackingNumber;
    }
    @Override public String eventType() { return "order.shipped"; }
    public UUID getOrderId()          { return orderId; }
    public String getOrderNumber()    { return orderNumber; }
    public UUID getCustomerId()       { return customerId; }
    public String getTrackingNumber() { return trackingNumber; }
}
