package com.ecommerce.payment.domain.repository;

import com.ecommerce.payment.domain.model.Payment;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(UUID id);
    Optional<Payment> findByIdempotencyKey(String key);
    Optional<Payment> findByOrderId(UUID orderId);
}
