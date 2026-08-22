package com.ecommerce.payment.infrastructure.persistence;

import com.ecommerce.payment.domain.model.Payment;
import com.ecommerce.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository @RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {
    private final JpaPaymentRepository jpa;
    @Override public Payment save(Payment p)                              { return jpa.save(p); }
    @Override public Optional<Payment> findById(UUID id)                  { return jpa.findById(id); }
    @Override public Optional<Payment> findByIdempotencyKey(String key)   { return jpa.findByIdempotencyKey(key); }
    @Override public Optional<Payment> findByOrderId(UUID orderId)        { return jpa.findByOrderId(orderId); }
}
