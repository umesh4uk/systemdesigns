package com.ecommerce.shared.domain.valueobject;

import com.ecommerce.shared.exception.DomainException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Phone number value object. Stores in E.164 format.
 */
public final class PhoneNumber {

    // Accepts optional leading + and 7-15 digits
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{6,14}$");

    private final String value;

    private PhoneNumber(String value) {
        this.value = value;
    }

    public static PhoneNumber of(String value) {
        Objects.requireNonNull(value, "phone number must not be null");
        String normalized = value.trim().replaceAll("[\\s\\-()]", "");
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new DomainException("Invalid phone number: " + value);
        }
        return new PhoneNumber(normalized);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhoneNumber other)) return false;
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
