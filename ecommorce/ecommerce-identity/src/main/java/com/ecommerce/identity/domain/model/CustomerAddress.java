package com.ecommerce.identity.domain.model;

import com.ecommerce.shared.domain.model.BaseEntity;
import com.ecommerce.shared.domain.valueobject.Address;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

/**
 * A saved address belonging to a customer.
 * Owned by the Customer aggregate — never persisted independently.
 */
@Getter
@Entity
@Table(name = "customer_addresses")
public class CustomerAddress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, updatable = false)
    private Customer customer;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "addressLine1", column = @Column(name = "address_line1", nullable = false)),
        @AttributeOverride(name = "addressLine2", column = @Column(name = "address_line2")),
        @AttributeOverride(name = "city",         column = @Column(name = "city", nullable = false)),
        @AttributeOverride(name = "state",        column = @Column(name = "state")),
        @AttributeOverride(name = "postalCode",   column = @Column(name = "postal_code", nullable = false)),
        @AttributeOverride(name = "country",      column = @Column(name = "country", nullable = false, length = 2))
    })
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 10)
    private AddressType addressType;

    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress;

    @Column(name = "label", length = 50)
    private String label;   // e.g. "Home", "Office"

    protected CustomerAddress() {}

    CustomerAddress(Customer customer, Address address, AddressType addressType,
                    boolean defaultAddress, String label) {
        super();
        this.customer = customer;
        this.address = address;
        this.addressType = addressType;
        this.defaultAddress = defaultAddress;
        this.label = label;
    }

    void markAsDefault() {
        this.defaultAddress = true;
    }

    void unmarkAsDefault() {
        this.defaultAddress = false;
    }

    public void update(Address newAddress, AddressType newType, String newLabel) {
        this.address = newAddress;
        this.addressType = newType;
        this.label = newLabel;
    }
}
