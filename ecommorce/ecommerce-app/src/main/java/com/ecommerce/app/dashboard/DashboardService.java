package com.ecommerce.app.dashboard;

import com.ecommerce.catalog.domain.model.ProductStatus;
import com.ecommerce.catalog.infrastructure.persistence.JpaProductRepository;
import com.ecommerce.identity.infrastructure.persistence.JpaCustomerRepository;
import com.ecommerce.inventory.infrastructure.persistence.JpaInventoryRepository;
import com.ecommerce.order.domain.model.OrderStatus;
import com.ecommerce.order.infrastructure.persistence.JpaOrderRepository;
import com.ecommerce.payment.infrastructure.persistence.JpaPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

/**
 * Aggregates metrics from multiple bounded-context JPA repositories
 * to produce the admin dashboard summary.
 *
 * <p>Accessing JPA repositories from multiple modules is intentional here:
 * {@code DashboardService} lives in {@code ecommerce-app}, which is the only
 * module permitted to reach across context boundaries for read-side aggregation.
 * No write operations or business logic cross context boundaries.
 *
 * <p>Results are cached for 60 seconds to avoid hammering the DB on every
 * page refresh.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JpaCustomerRepository  customerRepository;
    private final JpaProductRepository   productRepository;
    private final JpaOrderRepository     orderRepository;
    private final JpaInventoryRepository inventoryRepository;
    private final JpaPaymentRepository   paymentRepository;

    private static final Set<OrderStatus> REVENUE_STATUSES = Set.of(
            OrderStatus.CONFIRMED, OrderStatus.PROCESSING,
            OrderStatus.SHIPPED,   OrderStatus.DELIVERED,
            OrderStatus.REFUNDED,  OrderStatus.RETURN_REQUESTED,
            OrderStatus.RETURNED);

    @Transactional(readOnly = true)
    @Cacheable(value = "dashboard", key = "'summary'")
    public DashboardSummary getSummary() {
        Instant now       = Instant.now();
        Instant startOfDay = now.truncatedTo(ChronoUnit.DAYS);
        Instant thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);

        // ── customers ─────────────────────────────────────────────────────────
        long totalCustomers    = customerRepository.count();
        long activeCustomers   = customerRepository.countByStatus(
                com.ecommerce.identity.domain.model.CustomerStatus.ACTIVE);
        long newCustomersToday = customerRepository.countByCreatedAtAfter(startOfDay);

        // ── catalog ───────────────────────────────────────────────────────────
        long totalProducts  = productRepository.count();
        long activeProducts = productRepository.countByStatus(ProductStatus.ACTIVE);
        long draftProducts  = productRepository.countByStatus(ProductStatus.DRAFT);

        // ── orders ────────────────────────────────────────────────────────────
        long totalOrders      = orderRepository.count();
        long ordersToday      = orderRepository.countByCreatedAtAfter(startOfDay);
        long pendingOrders    = orderRepository.countByStatusIn(Set.of(
                OrderStatus.CREATED, OrderStatus.PAYMENT_PENDING, OrderStatus.PAYMENT_FAILED));
        long processingOrders = orderRepository.countByStatusIn(Set.of(
                OrderStatus.CONFIRMED, OrderStatus.PROCESSING));
        long shippedOrders    = orderRepository.countByStatus(OrderStatus.SHIPPED);
        long cancelledOrders  = orderRepository.countByStatus(OrderStatus.CANCELLED);

        // ── revenue ───────────────────────────────────────────────────────────
        BigDecimal totalRevenue      = orderRepository.sumTotalAmountByStatusIn(REVENUE_STATUSES);
        BigDecimal revenueToday      = orderRepository.sumTotalAmountByStatusInAndCreatedAtAfter(
                REVENUE_STATUSES, startOfDay);
        BigDecimal revenueLast30Days = orderRepository.sumTotalAmountByStatusInAndCreatedAtAfter(
                REVENUE_STATUSES, thirtyDaysAgo);

        // ── inventory alerts ─────────────────────────────────────────────────
        long lowStockSkuCount = inventoryRepository.countByAvailableQuantityLessThanEqualReorderThreshold();

        return new DashboardSummary(
                totalCustomers, activeCustomers, newCustomersToday,
                totalProducts, activeProducts, draftProducts,
                totalOrders, ordersToday, pendingOrders, processingOrders,
                shippedOrders, cancelledOrders,
                nullSafe(totalRevenue), nullSafe(revenueToday), nullSafe(revenueLast30Days),
                lowStockSkuCount,
                now);
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
