package com.ecommerce.identity.domain.repository;

import com.ecommerce.identity.domain.model.Customer;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository port (domain interface).
 * Implemented by the infrastructure layer (JPA adapter).
 */
public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(UUID id);

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);
}
