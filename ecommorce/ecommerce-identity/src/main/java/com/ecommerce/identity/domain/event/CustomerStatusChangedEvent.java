package com.ecommerce.identity.domain.event;

import com.ecommerce.identity.domain.model.CustomerStatus;
import com.ecommerce.shared.domain.event.BaseDomainEvent;

import java.util.UUID;

public final class CustomerStatusChangedEvent extends BaseDomainEvent {

    private final UUID customerId;
    private final CustomerStatus newStatus;

    public CustomerStatusChangedEvent(UUID customerId, CustomerStatus newStatus) {
        super();
        this.customerId = customerId;
        this.newStatus = newStatus;
    }

    @Override
    public String eventType() {
        return "customer.status.changed";
    }

    public UUID getCustomerId() { return customerId; }
    public CustomerStatus getNewStatus() { return newStatus; }
}
