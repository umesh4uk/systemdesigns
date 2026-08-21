package com.ecommerce.shared.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Marker interface for all domain events.
 * Every event carries a unique event ID and the time it occurred.
 * Implementations should be immutable records or final classes.
 */
public interface DomainEvent {

    /** Unique identifier for this event instance (for idempotent consumers). */
    UUID eventId();

    /** The instant at which this domain event occurred. */
    Instant occurredAt();

    /** Logical name of the event, used as Kafka topic suffix. */
    String eventType();
}
