package com.ecommerce.app.web;

import com.ecommerce.shared.api.response.ErrorResponse;
import com.ecommerce.shared.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.ecommerce.shared.util.MdcKeys;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Central exception-to-HTTP-response mapping.
 *
 * <p>Design invariants:
 * <ul>
 *   <li>Every response uses the canonical {@link ErrorResponse} shape.</li>
 *   <li>{@code code} is a machine-readable constant (e.g. {@code INSUFFICIENT_STOCK}).</li>
 *   <li>{@code correlationId} is always populated from SLF4J MDC.</li>
 *   <li>Stack traces, DB error messages, internal class names, and secrets are NEVER exposed.</li>
 *   <li>Generic 500 responses log the real cause server-side but return only a safe message.</li>
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Bean Validation ───────────────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        Map<String, List<String>> fieldErrors = ex.getBindingResult()
                .getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())));

        return ResponseEntity.unprocessableEntity()
                .body(ErrorResponse.withFieldErrors(
                        422, "VALIDATION_FAILED",
                        "Request validation failed — see fieldErrors for details",
                        req.getRequestURI(), correlationId(), fieldErrors));
    }

    // ── Domain / business rules ───────────────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error(404, "NOT_FOUND", ex.getMessage(), req));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error(409, "CONFLICT", ex.getMessage(), req));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(
            DomainException ex, HttpServletRequest req) {
        return ResponseEntity.unprocessableEntity()
                .body(error(422, "DOMAIN_RULE_VIOLATION", ex.getMessage(), req));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(
            BusinessRuleException ex, HttpServletRequest req) {
        // errorCode from the exception IS the machine-readable code
        return ResponseEntity.badRequest()
                .body(error(400, ex.getErrorCode(), ex.getMessage(), req));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(
            InsufficientStockException ex, HttpServletRequest req) {
        return ResponseEntity.unprocessableEntity()
                .body(error(422, "INSUFFICIENT_STOCK", ex.getMessage(), req));
    }

    // ── Security ──────────────────────────────────────────────────────────────

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleBadCredentials(HttpServletRequest req) {
        // Never reveal whether the email exists — same message for both cases
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(error(401, "INVALID_CREDENTIALS", "Invalid email or password", req));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(error(403, "ACCESS_DENIED",
                        "You do not have permission to perform this action", req));
    }

    // ── Catch-all (never expose internals) ────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest req) {
        // Log the full cause internally — return nothing sensitive externally
        log.error("Unhandled exception [correlationId={}] at {}: {}",
                correlationId(), req.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.internalServerError()
                .body(error(500, "INTERNAL_ERROR",
                        "An unexpected error occurred. Please try again later.", req));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static ErrorResponse error(int status, String code,
                                       String message, HttpServletRequest req) {
        return ErrorResponse.of(status, code, message, req.getRequestURI(), correlationId());
    }

    /** Pull correlationId from MDC — always present because CorrelationIdFilter runs first. */
    private static String correlationId() {
        String id = MDC.get(MdcKeys.CORRELATION_ID);
        return id != null ? id : "unknown";
    }
}
