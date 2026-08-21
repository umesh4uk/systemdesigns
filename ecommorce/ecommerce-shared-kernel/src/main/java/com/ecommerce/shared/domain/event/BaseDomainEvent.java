package com.ecommerce.shared.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Convenience base record for domain events.
 * Bounded-context event records can extend this or implement DomainEvent directly.
 */
public abstract class BaseDomainEvent implements DomainEvent {

    private final UUID eventId;
    private final Instant occurredAt;

    protected BaseDomainEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredAt = Instant.now();
    }

    @Override
    public UUID eventId() {
        return eventId;
    }

    @Override
    public Instant occurredAt() {
        return occurredAt;
    }
}
