package com.ecommerce.order.infrastructure.persistence;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaOrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") UUID id);

    // ── Dashboard queries ─────────────────────────────────────────────────────
    long countByCreatedAtAfter(java.time.Instant after);

    long countByStatus(OrderStatus status);

    long countByStatusIn(java.util.Collection<OrderStatus> statuses);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status IN :statuses")
    java.math.BigDecimal sumTotalAmountByStatusIn(
            @Param("statuses") java.util.Collection<OrderStatus> statuses);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
           "WHERE o.status IN :statuses AND o.createdAt > :after")
    java.math.BigDecimal sumTotalAmountByStatusInAndCreatedAtAfter(
            @Param("statuses") java.util.Collection<OrderStatus> statuses,
            @Param("after")    java.time.Instant after);
}
