package com.ecommerce.app.dashboard;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Aggregated dashboard snapshot for admin views.
 * Computed by {@link DashboardService} from multiple bounded-context repositories.
 */
public record DashboardSummary(

    // ── Customers ────────────────────────────────────────────
    long totalCustomers,
    long activeCustomers,
    long newCustomersToday,

    // ── Catalog ──────────────────────────────────────────────
    long totalProducts,
    long activeProducts,
    long draftProducts,

    // ── Orders ───────────────────────────────────────────────
    long totalOrders,
    long ordersToday,
    long pendingOrders,       // CREATED + PAYMENT_PENDING
    long processingOrders,    // CONFIRMED + PROCESSING
    long shippedOrders,
    long cancelledOrders,

    // ── Revenue ───────────────────────────────────────────────
    BigDecimal totalRevenue,        // all CONFIRMED+ orders
    BigDecimal revenueToday,
    BigDecimal revenueLast30Days,

    // ── Inventory alerts ─────────────────────────────────────
    long lowStockSkuCount,          // items at or below reorder threshold

    // ── Metadata ─────────────────────────────────────────────
    Instant generatedAt
) {}
