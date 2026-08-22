package com.ecommerce.order.domain.event;

import com.ecommerce.shared.domain.event.BaseDomainEvent;
import java.math.BigDecimal;
import java.util.UUID;

public final class OrderPlacedEvent extends BaseDomainEvent {
    private final UUID orderId;
    private final String orderNumber;
    private final UUID customerId;
    private final BigDecimal totalAmount;
    private final String currency;

    public OrderPlacedEvent(UUID orderId, String orderNumber, UUID customerId,
                             BigDecimal totalAmount, String currency) {
        super();
        this.orderId = orderId; this.orderNumber = orderNumber;
        this.customerId = customerId; this.totalAmount = totalAmount; this.currency = currency;
    }

    @Override public String eventType() { return "order.placed"; }
    public UUID getOrderId()          { return orderId; }
    public String getOrderNumber()    { return orderNumber; }
    public UUID getCustomerId()       { return customerId; }
    public BigDecimal getTotalAmount(){ return totalAmount; }
    public String getCurrency()       { return currency; }
}
