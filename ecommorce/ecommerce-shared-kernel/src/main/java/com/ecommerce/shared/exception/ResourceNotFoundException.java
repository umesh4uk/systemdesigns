package com.ecommerce.shared.exception;

/**
 * Thrown when a requested resource does not exist.
 * Always results in a 404 Not Found response at the API layer.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceType, Object id) {
        super(resourceType + " not found with id: " + id);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
