package com.ecommerce.order.infrastructure.persistence;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.model.OrderStatus;
import com.ecommerce.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {
    private final JpaOrderRepository jpa;

    @Override public Order save(Order order)                                                     { return jpa.save(order); }
    @Override public Optional<Order> findById(UUID id)                                           { return jpa.findByIdWithItems(id); }
    @Override public Optional<Order> findByOrderNumber(String num)                               { return jpa.findByOrderNumber(num); }
    @Override public Page<Order> findByCustomerId(UUID customerId, Pageable p)                   { return jpa.findByCustomerId(customerId, p); }
    @Override public Page<Order> findByStatus(OrderStatus status, Pageable p)                   { return jpa.findByStatus(status, p); }
    @Override public Page<Order> findAll(Pageable p)                                              { return jpa.findAll(p); }
}
