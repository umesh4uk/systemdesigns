package com.ecommerce.identity.domain.model;

/**
 * Lifecycle status of a customer account.
 */
public enum CustomerStatus {
    /** Account created but email not yet verified. */
    PENDING_VERIFICATION,
    /** Active, fully usable account. */
    ACTIVE,
    /** Suspended by admin — cannot log in. */
    SUSPENDED,
    /** Soft-deleted — data retained for audit. */
    DEACTIVATED
}
