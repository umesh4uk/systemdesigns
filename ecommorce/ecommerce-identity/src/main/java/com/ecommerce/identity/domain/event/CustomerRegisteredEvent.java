package com.ecommerce.identity.domain.event;

import com.ecommerce.shared.domain.event.BaseDomainEvent;

import java.util.UUID;

/**
 * Fired when a new customer successfully registers.
 * Consumed by: Notification (welcome email), Promotion (new-customer coupon).
 */
public final class CustomerRegisteredEvent extends BaseDomainEvent {

    private final UUID customerId;
    private final String email;

    public CustomerRegisteredEvent(UUID customerId, String email) {
        super();
        this.customerId = customerId;
        this.email = email;
    }

    @Override
    public String eventType() {
        return "customer.registered";
    }

    public UUID getCustomerId() { return customerId; }
    public String getEmail() { return email; }
}
