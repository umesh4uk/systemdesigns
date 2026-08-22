package com.ecommerce.shared.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Canonical error envelope returned for every API error.
 *
 * <pre>
 * {
 *   "timestamp":     "2026-08-21T12:00:00Z",
 *   "status":        400,
 *   "code":          "INVALID_REQUEST",
 *   "message":       "Invalid product quantity",
 *   "path":          "/api/v1/cart/items",
 *   "correlationId": "abc-123",
 *   "fieldErrors":   { "quantity": ["must be greater than 0"] }
 * }
 * </pre>
 *
 * <p>Design decisions:
 * <ul>
 *   <li>{@code code} is a machine-readable error code (e.g. {@code INSUFFICIENT_STOCK})
 *       that clients can switch on — separate from the human-readable {@code message}.</li>
 *   <li>{@code fieldErrors} is only present for Bean Validation (422) failures.</li>
 *   <li>Stack traces, DB details, and internal class names are never included.</li>
 *   <li>{@code correlationId} is always populated from SLF4J MDC.</li>
 * </ul>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int     status,
        String  code,
        String  message,
        String  path,
        String  correlationId,
        Map<String, List<String>> fieldErrors
) {
    // ── factory methods ───────────────────────────────────────────────────────

    public static ErrorResponse of(int status, String code, String message,
                                   String path, String correlationId) {
        return new ErrorResponse(Instant.now(), status, code, message,
                                 path, correlationId, null);
    }

    public static ErrorResponse withFieldErrors(int status, String code, String message,
                                                String path, String correlationId,
                                                Map<String, List<String>> fieldErrors) {
        return new ErrorResponse(Instant.now(), status, code, message,
                                 path, correlationId, fieldErrors);
    }
}
