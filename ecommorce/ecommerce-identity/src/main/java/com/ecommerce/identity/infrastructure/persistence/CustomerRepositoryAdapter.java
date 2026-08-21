package com.ecommerce.identity.infrastructure.persistence;

import com.ecommerce.identity.domain.model.Customer;
import com.ecommerce.identity.domain.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that bridges the domain repository port to the JPA repository.
 * Keeps JPA out of the domain layer.
 */
@Repository
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final JpaCustomerRepository jpa;

    @Override
    public Customer save(Customer customer) {
        return jpa.save(customer);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return jpa.findByIdWithAddresses(id);
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return jpa.findByEmail(email.toLowerCase());
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email.toLowerCase());
    }
}
