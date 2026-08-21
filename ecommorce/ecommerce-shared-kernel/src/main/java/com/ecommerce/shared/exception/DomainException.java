package com.ecommerce.shared.exception;

/**
 * Thrown when a domain invariant is violated.
 * Always results in a 422 Unprocessable Entity response at the API layer.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
