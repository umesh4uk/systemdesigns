package com.ecommerce.app.web;

import com.ecommerce.shared.util.CorrelationIdHolder;
import com.ecommerce.shared.util.MdcKeys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Runs before every request to:
 * <ol>
 *   <li>Extract or generate a correlation ID from the {@code X-Correlation-Id} header.</li>
 *   <li>Store it in SLF4J {@link MDC} so all log lines for this request carry it.</li>
 *   <li>Store it in {@link CorrelationIdHolder} for non-logging code paths (e.g. error responses).</li>
 *   <li>Echo it back in the response header.</li>
 *   <li>Populate HTTP method and path into MDC for structured log queries.</li>
 *   <li>Clean up MDC on completion — prevents MDC leaks in thread-pool environments.</li>
 * </ol>
 *
 * <p>Runs at {@link Order#HIGHEST_PRECEDENCE} so MDC is available even if later filters throw.
 */
@Component
@Order(Integer.MIN_VALUE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest  request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain         chain)
            throws ServletException, IOException {

        String correlationId = request.getHeader(CORRELATION_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // ── populate MDC ──────────────────────────────────────────────────────
        MDC.put(MdcKeys.CORRELATION_ID, correlationId);
        MDC.put(MdcKeys.HTTP_METHOD,    request.getMethod());
        MDC.put(MdcKeys.HTTP_PATH,      request.getRequestURI());

        // Legacy ThreadLocal (used by ErrorResponse factories)
        CorrelationIdHolder.set(correlationId);

        // Echo back so clients can correlate their request with log entries
        response.setHeader(CORRELATION_HEADER, correlationId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MdcKeys.CORRELATION_ID);
            MDC.remove(MdcKeys.CUSTOMER_ID);
            MDC.remove(MdcKeys.HTTP_METHOD);
            MDC.remove(MdcKeys.HTTP_PATH);
            MDC.remove(MdcKeys.USER_ROLE);
            CorrelationIdHolder.clear();
        }
    }
}
