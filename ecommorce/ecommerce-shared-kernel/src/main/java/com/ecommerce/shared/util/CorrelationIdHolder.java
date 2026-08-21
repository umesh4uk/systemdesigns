package com.ecommerce.shared.util;

import java.util.UUID;

/**
 * Thread-local holder for the current request's correlation ID.
 * Set by the CorrelationIdFilter in ecommerce-app and read by
 * error handlers and event publishers throughout the call stack.
 */
public final class CorrelationIdHolder {

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private CorrelationIdHolder() {}

    public static void set(String correlationId) {
        HOLDER.set(correlationId);
    }

    public static String get() {
        String id = HOLDER.get();
        return id != null ? id : UUID.randomUUID().toString();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
