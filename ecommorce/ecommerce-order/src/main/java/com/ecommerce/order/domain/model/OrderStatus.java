package com.ecommerce.order.domain.model;

import com.ecommerce.shared.exception.BusinessRuleException;

import java.util.Set;

public enum OrderStatus {
    CREATED,
    PAYMENT_PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    PAYMENT_FAILED,
    RETURN_REQUESTED,
    RETURNED,
    REFUNDED;

    /** Valid transitions enforced by the aggregate. */
    private static final java.util.Map<OrderStatus, Set<OrderStatus>> TRANSITIONS =
            new java.util.EnumMap<>(OrderStatus.class);

    static {
        TRANSITIONS.put(CREATED,          Set.of(PAYMENT_PENDING, CANCELLED));
        TRANSITIONS.put(PAYMENT_PENDING,  Set.of(CONFIRMED, PAYMENT_FAILED, CANCELLED));
        TRANSITIONS.put(PAYMENT_FAILED,   Set.of(PAYMENT_PENDING, CANCELLED));
        TRANSITIONS.put(CONFIRMED,        Set.of(PROCESSING, CANCELLED));
        TRANSITIONS.put(PROCESSING,       Set.of(SHIPPED, CANCELLED));
        TRANSITIONS.put(SHIPPED,          Set.of(DELIVERED));
        TRANSITIONS.put(DELIVERED,        Set.of(RETURN_REQUESTED));
        TRANSITIONS.put(RETURN_REQUESTED, Set.of(RETURNED, DELIVERED));
        TRANSITIONS.put(RETURNED,         Set.of(REFUNDED));
        TRANSITIONS.put(CANCELLED,        Set.of());
        TRANSITIONS.put(REFUNDED,         Set.of());
    }

    public void validateTransition(OrderStatus next) {
        if (!TRANSITIONS.getOrDefault(this, Set.of()).contains(next)) {
            throw new BusinessRuleException("INVALID_ORDER_TRANSITION",
                    "Cannot transition order from " + this + " to " + next);
        }
    }
}
