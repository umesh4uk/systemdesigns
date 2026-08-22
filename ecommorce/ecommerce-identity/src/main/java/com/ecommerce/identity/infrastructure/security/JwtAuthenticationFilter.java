package com.ecommerce.identity.infrastructure.security;

import com.ecommerce.shared.util.CorrelationIdHolder;
import com.ecommerce.shared.util.MdcKeys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Stateless JWT filter.
 *
 * <p>On every request carrying a {@code Authorization: Bearer <token>} header:
 * <ol>
 *   <li>Validates the JWT signature, expiry, and issuer.</li>
 *   <li>Rejects refresh tokens from being used as access tokens.</li>
 *   <li>Populates the Spring {@code SecurityContext} with the authenticated principal.</li>
 *   <li>Enriches SLF4J MDC with {@code customer.id} and {@code user.role} for structured logging.</li>
 * </ol>
 *
 * <p>Security notes:
 * <ul>
 *   <li>Invalid tokens are silently skipped — the request continues unauthenticated.
 *       Endpoints that require auth will return 401 from the access-denied handler.</li>
 *   <li>No session is created — purely stateless.</li>
 *   <li>Credentials are never logged.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX      = "Bearer ";
    private static final String AUTH_HEADER        = "Authorization";

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest  request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain         chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        try {
            Claims claims = tokenProvider.validateAndParseClaims(token);

            // Reject refresh tokens used as access tokens
            if ("refresh".equals(claims.get("type", String.class))) {
                chain.doFilter(request, response);
                return;
            }

            @SuppressWarnings("unchecked")
            List<String> roles = claims.get("roles", List.class);
            List<SimpleGrantedAuthority> authorities = roles == null ? List.of() :
                    roles.stream().map(SimpleGrantedAuthority::new).toList();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            claims.getSubject(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Enrich MDC — never log the token itself
            MDC.put(MdcKeys.CUSTOMER_ID, claims.getSubject());
            if (!authorities.isEmpty()) {
                MDC.put(MdcKeys.USER_ROLE, authorities.get(0).getAuthority());
            }

        } catch (JwtException e) {
            // Deliberately vague — do not reveal why the token failed
            log.debug("JWT validation failed for request to {}", request.getRequestURI());
        }

        chain.doFilter(request, response);
    }
}
