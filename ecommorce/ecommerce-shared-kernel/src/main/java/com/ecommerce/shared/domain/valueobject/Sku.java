package com.ecommerce.shared.domain.valueobject;

import com.ecommerce.shared.exception.DomainException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Stock Keeping Unit (SKU) value object.
 * Format: uppercase alphanumeric and hyphens, 3–64 chars.
 */
public final class Sku {

    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Z0-9\\-]{3,64}$");

    private final String value;

    private Sku(String value) {
        this.value = value;
    }

    public static Sku of(String value) {
        Objects.requireNonNull(value, "SKU must not be null");
        String normalized = value.trim().toUpperCase();
        if (!SKU_PATTERN.matcher(normalized).matches()) {
            throw new DomainException("Invalid SKU format: " + value
                    + ". Must be uppercase alphanumeric with hyphens, 3-64 chars.");
        }
        return new Sku(normalized);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sku other)) return false;
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
