package com.ecommerce.shared.security;

/**
 * Canonical role constants used throughout the platform.
 *
 * <p>Spring Security uses the {@code ROLE_} prefix convention; these constants
 * omit it so callers can use both {@code hasRole("ADMIN")} and
 * {@code hasAuthority("ROLE_ADMIN")} interchangeably.
 *
 * <p>Why four roles?
 * <ul>
 *   <li>{@link #CUSTOMER} — end-user, can only access their own resources.</li>
 *   <li>{@link #ADMIN} — full platform administration.</li>
 *   <li>{@link #INVENTORY_MANAGER} — can manage stock levels and warehouse data
 *       but cannot access customer PII or financials.</li>
 *   <li>{@link #ORDER_MANAGER} — can view/update order status and manage
 *       fulfilment but cannot modify product catalog or pricing.</li>
 * </ul>
 *
 * <p>Principle of least privilege: a logged-in staff member gets only the role
 * that covers their job function, not blanket ADMIN access.
 */
public final class AppRole {

    private AppRole() {}

    public static final String CUSTOMER          = "CUSTOMER";
    public static final String ADMIN             = "ADMIN";
    public static final String INVENTORY_MANAGER = "INVENTORY_MANAGER";
    public static final String ORDER_MANAGER     = "ORDER_MANAGER";

    /** Spring Security authority string (with prefix). */
    public static final String ROLE_CUSTOMER          = "ROLE_CUSTOMER";
    public static final String ROLE_ADMIN             = "ROLE_ADMIN";
    public static final String ROLE_INVENTORY_MANAGER = "ROLE_INVENTORY_MANAGER";
    public static final String ROLE_ORDER_MANAGER     = "ROLE_ORDER_MANAGER";
}
