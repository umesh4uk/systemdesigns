package com.ecommerce.identity.application.service;

import com.ecommerce.identity.application.dto.*;
import com.ecommerce.identity.application.mapper.CustomerMapper;
import com.ecommerce.identity.domain.model.Customer;
import com.ecommerce.identity.domain.repository.CustomerRepository;
import com.ecommerce.shared.domain.valueobject.Address;
import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Customer profile and address management use cases.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(UUID customerId) {
        return customerMapper.toResponse(loadCustomer(customerId));
    }

    @Transactional
    public CustomerResponse updateProfile(UUID customerId, UpdateProfileRequest request) {
        Customer customer = loadCustomer(customerId);
        customer.updateProfile(request.firstName(), request.lastName(), request.phone());
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Transactional
    public void changePassword(UUID customerId, ChangePasswordRequest request) {
        Customer customer = loadCustomer(customerId);
        if (!passwordEncoder.matches(request.currentPassword(), customer.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        customer.changePassword(passwordEncoder.encode(request.newPassword()));
        customerRepository.save(customer);
        log.info("Password changed for customer: {}", customerId);
    }

    // ------------------------------------------------------------------ addresses

    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(UUID customerId) {
        Customer customer = loadCustomer(customerId);
        return customer.getAddresses().stream()
                .map(customerMapper::toAddressResponse)
                .toList();
    }

    @Transactional
    public AddressResponse addAddress(UUID customerId, AddressRequest request) {
        Customer customer = loadCustomer(customerId);

        Address address = Address.builder()
                .addressLine1(request.addressLine1())
                .addressLine2(request.addressLine2())
                .city(request.city())
                .state(request.state())
                .postalCode(request.postalCode())
                .country(request.country())
                .build();

        var saved = customer.addAddress(address, request.addressType(),
                request.defaultAddress(), request.label());
        customerRepository.save(customer);
        return customerMapper.toAddressResponse(saved);
    }

    @Transactional
    public AddressResponse updateAddress(UUID customerId, UUID addressId, AddressRequest request) {
        Customer customer = loadCustomer(customerId);

        Address newAddress = Address.builder()
                .addressLine1(request.addressLine1())
                .addressLine2(request.addressLine2())
                .city(request.city())
                .state(request.state())
                .postalCode(request.postalCode())
                .country(request.country())
                .build();

        customer.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId))
                .update(newAddress, request.addressType(), request.label());

        customerRepository.save(customer);
        return customerMapper.toAddressResponse(
                customer.getAddresses().stream()
                        .filter(a -> a.getId().equals(addressId))
                        .findFirst().orElseThrow());
    }

    @Transactional
    public void setDefaultAddress(UUID customerId, UUID addressId) {
        Customer customer = loadCustomer(customerId);
        customer.setDefaultAddress(addressId);
        customerRepository.save(customer);
    }

    @Transactional
    public void removeAddress(UUID customerId, UUID addressId) {
        Customer customer = loadCustomer(customerId);
        customer.removeAddress(addressId);
        customerRepository.save(customer);
    }

    // ------------------------------------------------------------------ admin

    @Transactional
    public CustomerResponse suspendCustomer(UUID customerId) {
        Customer customer = loadCustomer(customerId);
        customer.suspend();
        Customer saved = customerRepository.save(customer);
        publishEvents(saved);
        return customerMapper.toResponse(saved);
    }

    @Transactional
    public CustomerResponse activateCustomer(UUID customerId) {
        Customer customer = loadCustomer(customerId);
        customer.activate();
        Customer saved = customerRepository.save(customer);
        publishEvents(saved);
        return customerMapper.toResponse(saved);
    }

    /**
     * Assign a new platform role to a customer account.
     * Only admins can call this; enforced at the controller layer via {@code @PreAuthorize}.
     *
     * <p>Why here and not in AuthService? Role management is a customer-profile concern,
     * not an authentication concern. AuthService only handles credential flows.
     */
    @Transactional
    public CustomerResponse changeRole(UUID customerId, String newRole) {
        // Validate the role is one we know about
        java.util.Set<String> valid = java.util.Set.of(
                com.ecommerce.shared.security.AppRole.CUSTOMER,
                com.ecommerce.shared.security.AppRole.ADMIN,
                com.ecommerce.shared.security.AppRole.INVENTORY_MANAGER,
                com.ecommerce.shared.security.AppRole.ORDER_MANAGER);
        if (!valid.contains(newRole)) {
            throw new com.ecommerce.shared.exception.BusinessRuleException(
                    "INVALID_ROLE", "Unknown role: " + newRole
                            + ". Must be one of: " + valid);
        }
        Customer customer = loadCustomer(customerId);
        customer.changeRole(newRole);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    // ------------------------------------------------------------------ helpers

    private Customer loadCustomer(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    private void publishEvents(Customer customer) {
        customer.getDomainEvents().forEach(eventPublisher::publishEvent);
        customer.clearDomainEvents();
    }
}
