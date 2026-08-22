package com.ecommerce.identity.infrastructure.persistence;

import com.ecommerce.identity.domain.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository — infrastructure concern.
 * The domain only depends on {@link com.ecommerce.identity.domain.repository.CustomerRepository}.
 */
public interface JpaCustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.addresses WHERE c.id = :id")
    Optional<Customer> findByIdWithAddresses(@Param("id") UUID id);

    // ── Dashboard queries ─────────────────────────────────────────────────────
    long countByStatus(com.ecommerce.identity.domain.model.CustomerStatus status);

    long countByCreatedAtAfter(java.time.Instant after);
}
