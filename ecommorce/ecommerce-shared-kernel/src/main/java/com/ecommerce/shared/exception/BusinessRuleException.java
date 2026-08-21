package com.ecommerce.shared.exception;

/**
 * Thrown when a business rule is violated (e.g. cancelling a delivered order,
 * applying an expired coupon). Results in 400 Bad Request at the API layer.
 */
public class BusinessRuleException extends RuntimeException {

    private final String errorCode;

    public BusinessRuleException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
