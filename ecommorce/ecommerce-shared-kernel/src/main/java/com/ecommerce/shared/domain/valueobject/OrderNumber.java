package com.ecommerce.shared.domain.valueobject;

import com.ecommerce.shared.exception.DomainException;

import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Human-readable order number value object.
 * Format: ORD-YYYYMMDD-XXXXXXXX (8 random hex chars).
 */
public final class OrderNumber {

    private static final String PREFIX = "ORD";

    private final String value;

    private OrderNumber(String value) {
        this.value = value;
    }

    /** Generate a new unique order number. */
    public static OrderNumber generate() {
        String date = LocalDate.now().toString().replace("-", "");
        String random = String.format("%08X", ThreadLocalRandom.current().nextInt());
        return new OrderNumber(PREFIX + "-" + date + "-" + random);
    }

    /** Reconstruct from stored string. */
    public static OrderNumber of(String value) {
        Objects.requireNonNull(value, "order number must not be null");
        if (!value.startsWith(PREFIX + "-")) {
            throw new DomainException("Invalid order number format: " + value);
        }
        return new OrderNumber(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderNumber other)) return false;
        return value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
