package com.ecommerce.shared.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Structured error response returned for all API errors.
 * Field-level validation errors are included in the {@code fieldErrors} map.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp,
        String correlationId,
        Map<String, List<String>> fieldErrors
) {
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(status, error, message, path, Instant.now(), null, null);
    }

    public static ErrorResponse of(int status, String error, String message, String path,
                                   String correlationId, Map<String, List<String>> fieldErrors) {
        return new ErrorResponse(status, error, message, path, Instant.now(), correlationId, fieldErrors);
    }
}
