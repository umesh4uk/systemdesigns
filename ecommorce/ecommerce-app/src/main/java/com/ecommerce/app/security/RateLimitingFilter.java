package com.ecommerce.app.security;

import com.ecommerce.shared.api.response.ErrorResponse;
import com.ecommerce.shared.util.MdcKeys;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-IP rate limiter protecting high-risk unauthenticated endpoints.
 *
 * <p><b>Why Bucket4j token-bucket?</b> Token-bucket allows short bursts (up to the
 * bucket capacity) while enforcing a long-run average rate — more user-friendly than
 * a strict fixed-window counter. Bucket capacity = 20 requests; refill = 10 req/min.
 *
 * <p><b>Protected paths:</b>
 * <ul>
 *   <li>{@code POST /api/v1/auth/login}  — prevents credential stuffing</li>
 *   <li>{@code POST /api/v1/auth/register} — prevents bulk account creation</li>
 *   <li>{@code POST /api/v1/auth/refresh} — prevents token-refresh flooding</li>
 * </ul>
 *
 * <p><b>In-process buckets</b> are used here (ConcurrentHashMap per IP). For a
 * horizontally-scaled deployment, swap the bucket store for
 * {@code Bucket4j-Redis} or {@code Bucket4j-Hazelcast} so buckets are shared
 * across all application instances.
 *
 * <p><b>Non-blocking:</b> if the request is not rate-limited it passes straight
 * through with no overhead.
 *
 * <p>Returns {@code 429 Too Many Requests} with a {@code Retry-After} header.
 */
@Slf4j
@Component
@Order(Integer.MIN_VALUE + 1)          // just after CorrelationIdFilter
public class RateLimitingFilter extends OncePerRequestFilter {

    /** Paths that are rate-limited.  Lower-cased for quick comparison. */
    private static final java.util.Set<String> PROTECTED_PATHS = java.util.Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh"
    );

    /** Per-IP token buckets. Max 10,000 tracked IPs before oldest are evicted. */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    public RateLimitingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !PROTECTED_PATHS.contains(request.getRequestURI().toLowerCase());
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest  request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain         chain)
            throws ServletException, IOException {

        String clientIp = resolveClientIp(request);
        Bucket bucket   = buckets.computeIfAbsent(clientIp, this::newBucket);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(request, response);
        } else {
            long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000L;
            log.warn("Rate limit exceeded [ip={}, path={}, correlationId={}]",
                    clientIp, request.getRequestURI(),
                    MDC.get(MdcKeys.CORRELATION_ID));

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setHeader("X-Rate-Limit-Remaining", "0");

            ErrorResponse body = ErrorResponse.of(
                    429, "RATE_LIMIT_EXCEEDED",
                    "Too many requests. Please retry after " + retryAfterSeconds + " seconds.",
                    request.getRequestURI(),
                    MDC.get(MdcKeys.CORRELATION_ID));

            objectMapper.writeValue(response.getWriter(), body);
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Bucket newBucket(String ip) {
        // Allow burst of 20, then refill 10 tokens per minute
        Bandwidth limit = Bandwidth.builder()
                .capacity(20)
                .refillGreedy(10, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    /**
     * Resolve real client IP, honouring the {@code X-Forwarded-For} header
     * set by load balancers and reverse proxies.
     * Returns {@code "unknown"} as a fallback so the bucket still works.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        String remote = request.getRemoteAddr();
        return remote != null ? remote : "unknown";
    }
}
