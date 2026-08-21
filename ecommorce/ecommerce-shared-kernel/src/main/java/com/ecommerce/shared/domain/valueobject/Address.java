package com.ecommerce.shared.domain.valueobject;

import com.ecommerce.shared.exception.DomainException;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.Objects;

/**
 * Address value object, embeddable into JPA entities.
 * Immutable — create a new instance for any change.
 */
@Getter
@Embeddable
public final class Address {

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    /** JPA requires no-arg constructor for embeddables. */
    protected Address() {}

    private Address(Builder builder) {
        this.addressLine1 = builder.addressLine1;
        this.addressLine2 = builder.addressLine2;
        this.city = builder.city;
        this.state = builder.state;
        this.postalCode = builder.postalCode;
        this.country = builder.country;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;

        public Builder addressLine1(String addressLine1) { this.addressLine1 = addressLine1; return this; }
        public Builder addressLine2(String addressLine2) { this.addressLine2 = addressLine2; return this; }
        public Builder city(String city) { this.city = city; return this; }
        public Builder state(String state) { this.state = state; return this; }
        public Builder postalCode(String postalCode) { this.postalCode = postalCode; return this; }
        public Builder country(String country) { this.country = country; return this; }

        public Address build() {
            if (addressLine1 == null || addressLine1.isBlank()) {
                throw new DomainException("addressLine1 is required");
            }
            if (city == null || city.isBlank()) {
                throw new DomainException("city is required");
            }
            if (country == null || country.isBlank()) {
                throw new DomainException("country is required");
            }
            if (postalCode == null || postalCode.isBlank()) {
                throw new DomainException("postalCode is required");
            }
            return new Address(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address other)) return false;
        return Objects.equals(addressLine1, other.addressLine1)
                && Objects.equals(addressLine2, other.addressLine2)
                && Objects.equals(city, other.city)
                && Objects.equals(state, other.state)
                && Objects.equals(postalCode, other.postalCode)
                && Objects.equals(country, other.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressLine1, addressLine2, city, state, postalCode, country);
    }

    @Override
    public String toString() {
        return addressLine1 + (addressLine2 != null ? ", " + addressLine2 : "")
                + ", " + city + ", " + state + " " + postalCode + ", " + country;
    }
}
