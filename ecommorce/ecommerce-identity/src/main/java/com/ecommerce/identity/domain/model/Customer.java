package com.ecommerce.identity.domain.model;

import com.ecommerce.identity.domain.event.CustomerRegisteredEvent;
import com.ecommerce.identity.domain.event.CustomerStatusChangedEvent;
import com.ecommerce.shared.domain.model.AggregateRoot;
import com.ecommerce.shared.domain.valueobject.Address;
import com.ecommerce.shared.domain.valueobject.EmailAddress;
import com.ecommerce.shared.domain.valueobject.PhoneNumber;
import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import com.ecommerce.shared.security.AppRole;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Customer aggregate root.
 *
 * <p>Enforces all identity/customer invariants:
 * <ul>
 *   <li>Email is unique and immutable after registration.</li>
 *   <li>Password hash is never exposed — only set/changed through methods.</li>
 *   <li>At most one default address per type.</li>
 *   <li>Status transitions follow a defined state machine.</li>
 * </ul>
 */
@Getter
@Entity
@Table(name = "customers",
        uniqueConstraints = @UniqueConstraint(name = "uq_customer_email", columnNames = "email"))
public class Customer extends AggregateRoot {

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;           // stored as lowercase string; use EmailAddress VO for logic

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CustomerStatus status;

    /**
     * The role assigned to this account — determines what they can do in the platform.
     * Stored as a string so new roles can be added without an enum migration.
     * Defaults to {@link AppRole#CUSTOMER} for all self-registered accounts.
     * Staff roles (ADMIN, INVENTORY_MANAGER, ORDER_MANAGER) are set by an existing ADMIN.
     */
    @Column(name = "role", nullable = false, length = 30)
    private String role = AppRole.CUSTOMER;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final List<CustomerAddress> addresses = new ArrayList<>();

    protected Customer() {}

    /**
     * Factory method — the only way to register a new customer.
     */
    public static Customer register(String email, String passwordHash,
                                    String firstName, String lastName, String phone) {
        Customer customer = new Customer();
        customer.email = EmailAddress.of(email).getValue();   // validates format
        customer.passwordHash = passwordHash;
        customer.firstName = firstName;
        customer.lastName = lastName;
        customer.phone = phone != null ? PhoneNumber.of(phone).getValue() : null;
        customer.status = CustomerStatus.ACTIVE;  // simplified: skip email verification flow for now
        customer.registerEvent(new CustomerRegisteredEvent(customer.getId(), customer.email));
        return customer;
    }

    // ------------------------------------------------------------------ profile

    public void updateProfile(String firstName, String lastName, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone != null ? PhoneNumber.of(phone).getValue() : null;
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    /**
     * Assign a platform role to this customer account.
     * Only an ADMIN can call this — enforced at the application-service layer.
     *
     * @param newRole one of {@link AppRole}'s constants
     */
    public void changeRole(String newRole) {
        this.role = newRole;
    }

    // ------------------------------------------------------------------ status

    public void activate() {
        if (status == CustomerStatus.DEACTIVATED) {
            throw new BusinessRuleException("CUSTOMER_DEACTIVATED", "Cannot activate a deactivated customer");
        }
        this.status = CustomerStatus.ACTIVE;
        registerEvent(new CustomerStatusChangedEvent(getId(), CustomerStatus.ACTIVE));
    }

    public void suspend() {
        if (status == CustomerStatus.DEACTIVATED) {
            throw new BusinessRuleException("CUSTOMER_DEACTIVATED", "Cannot suspend a deactivated customer");
        }
        this.status = CustomerStatus.SUSPENDED;
        registerEvent(new CustomerStatusChangedEvent(getId(), CustomerStatus.SUSPENDED));
    }

    public void deactivate() {
        this.status = CustomerStatus.DEACTIVATED;
        registerEvent(new CustomerStatusChangedEvent(getId(), CustomerStatus.DEACTIVATED));
    }

    public boolean isActive() {
        return status == CustomerStatus.ACTIVE;
    }

    // ------------------------------------------------------------------ addresses

    public CustomerAddress addAddress(Address address, AddressType type,
                                      boolean makeDefault, String label) {
        if (makeDefault) {
            clearDefaultForType(type);
        }
        boolean isFirst = addresses.stream().noneMatch(a -> a.getAddressType() == type
                || a.getAddressType() == AddressType.BOTH);
        CustomerAddress customerAddress = new CustomerAddress(this, address, type,
                makeDefault || isFirst, label);
        addresses.add(customerAddress);
        return customerAddress;
    }

    public void setDefaultAddress(UUID addressId) {
        CustomerAddress target = findAddressById(addressId);
        clearDefaultForType(target.getAddressType());
        target.markAsDefault();
    }

    public void removeAddress(UUID addressId) {
        CustomerAddress target = findAddressById(addressId);
        if (target.isDefaultAddress()) {
            throw new BusinessRuleException("DEFAULT_ADDRESS_REMOVAL",
                    "Cannot remove default address. Set another address as default first.");
        }
        addresses.remove(target);
    }

    public List<CustomerAddress> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    private void clearDefaultForType(AddressType type) {
        addresses.stream()
                .filter(a -> a.isDefaultAddress()
                        && (a.getAddressType() == type || a.getAddressType() == AddressType.BOTH))
                .forEach(CustomerAddress::unmarkAsDefault);
    }

    private CustomerAddress findAddressById(UUID addressId) {
        return addresses.stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CustomerAddress", addressId));
    }
}
