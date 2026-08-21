package com.ecommerce.shared.util;

import com.ecommerce.shared.exception.DomainException;

/**
 * Utility for fail-fast validation guards used in domain constructors.
 * Keeps domain classes free of repetitive null/blank checks.
 */
public final class ValidationUtils {

    private ValidationUtils() {}

    public static void requireNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new DomainException(fieldName + " must not be null");
        }
    }

    public static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainException(fieldName + " must not be blank");
        }
    }

    public static void requirePositive(Number value, String fieldName) {
        requireNotNull(value, fieldName);
        if (value.doubleValue() <= 0) {
            throw new DomainException(fieldName + " must be positive, got: " + value);
        }
    }

    public static void requireNonNegative(Number value, String fieldName) {
        requireNotNull(value, fieldName);
        if (value.doubleValue() < 0) {
            throw new DomainException(fieldName + " must not be negative, got: " + value);
        }
    }

    public static void requireTrue(boolean condition, String message) {
        if (!condition) {
            throw new DomainException(message);
        }
    }
}
