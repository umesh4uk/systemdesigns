package com.ecommerce.shared.exception;

/**
 * Thrown on resource conflicts (duplicate email, duplicate SKU, etc.).
 * Results in 409 Conflict at the API layer.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
