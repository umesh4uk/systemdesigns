package com.ecommerce.shared.util;

/**
 * Centralised MDC (Mapped Diagnostic Context) key constants.
 *
 * <p>Every filter and service that enriches the log context uses these keys so
 * that log aggregation tools (Loki, ELK, CloudWatch) can query consistently.
 *
 * <p>Convention: lowercase, dot-separated — matches ECS (Elastic Common Schema).
 */
public final class MdcKeys {

    private MdcKeys() {}

    /** Unique identifier for the current HTTP request, echoed in X-Correlation-Id header. */
    public static final String CORRELATION_ID  = "correlation.id";

    /** Authenticated customer UUID — populated after JWT validation. */
    public static final String CUSTOMER_ID     = "customer.id";

    /** HTTP method (GET, POST, etc.). */
    public static final String HTTP_METHOD     = "http.method";

    /** Request URI path. */
    public static final String HTTP_PATH       = "http.path";

    /** Role of the authenticated principal (CUSTOMER / ADMIN / etc.). */
    public static final String USER_ROLE       = "user.role";
}
