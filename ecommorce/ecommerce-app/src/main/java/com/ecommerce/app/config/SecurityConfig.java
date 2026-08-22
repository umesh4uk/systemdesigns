package com.ecommerce.app.config;

import com.ecommerce.identity.infrastructure.security.JwtAuthenticationFilter;
import com.ecommerce.shared.security.AppRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Central Spring Security configuration.
 *
 * <p><b>Authentication:</b> stateless JWT — no sessions, no CSRF token needed for pure REST.
 *
 * <p><b>Authorisation model</b> (principle of least privilege):
 * <ul>
 *   <li>Public read endpoints require no auth.</li>
 *   <li>{@code CUSTOMER} — can access their own cart, orders, profile.</li>
 *   <li>{@code ORDER_MANAGER} — can view/update any order status.</li>
 *   <li>{@code INVENTORY_MANAGER} — can manage stock levels.</li>
 *   <li>{@code ADMIN} — full access including user management and pricing.</li>
 * </ul>
 *
 * <p><b>Resource ownership</b> is enforced at the service layer, not just URL level.
 * A customer with a valid token can still only read their own orders.
 *
 * <p><b>Secure HTTP headers</b> follow OWASP recommendations:
 * <ul>
 *   <li>Content-Security-Policy — prevents XSS on any HTML responses.</li>
 *   <li>X-Frame-Options DENY — prevents clickjacking.</li>
 *   <li>X-Content-Type-Options nosniff — prevents MIME sniffing attacks.</li>
 *   <li>Referrer-Policy strict-origin-when-cross-origin — limits referrer leakage.</li>
 *   <li>Permissions-Policy — disables unused browser features.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ── Session / CSRF ────────────────────────────────────────────────
            .csrf(AbstractHttpConfigurer::disable)            // stateless JWT — no session
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── Secure HTTP response headers ──────────────────────────────────
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'none'; " +
                        "script-src 'self'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'none'"))
                .frameOptions(fo -> fo.deny())               // X-Frame-Options: DENY
                .contentTypeOptions(ct -> {})                // X-Content-Type-Options: nosniff
                .referrerPolicy(rp -> rp
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy
                            .STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .permissionsPolicy(pp -> pp
                    .policy("camera=(), microphone=(), geolocation=(), payment=()"))
            )

            // ── URL-level authorisation ───────────────────────────────────────
            .authorizeHttpRequests(auth -> auth

                // ── Public endpoints (no token required) ─────────────────────
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/v1/products/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/v1/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/v1/inventory/*/available").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**",
                                 "/swagger-ui.html").permitAll()

                // ── Inventory management (INVENTORY_MANAGER or ADMIN) ─────────
                .requestMatchers("/api/v1/inventory/**")
                    .hasAnyRole(AppRole.INVENTORY_MANAGER, AppRole.ADMIN)

                // ── Order management (ORDER_MANAGER or ADMIN) ─────────────────
                .requestMatchers("/api/v1/admin/orders/**")
                    .hasAnyRole(AppRole.ORDER_MANAGER, AppRole.ADMIN)

                // ── Full admin section (ADMIN only) ────────────────────────────
                .requestMatchers("/api/v1/admin/**")
                    .hasRole(AppRole.ADMIN)

                // ── Actuator sensitive endpoints (ADMIN only) ─────────────────
                .requestMatchers("/actuator/**").hasRole(AppRole.ADMIN)

                // ── Everything else — authenticated, any role ─────────────────
                .anyRequest().authenticated()
            )

            // ── JWT filter ────────────────────────────────────────────────────
            .addFilterBefore(jwtAuthenticationFilter,
                             UsernamePasswordAuthenticationFilter.class)

            // ── 401 / 403 handlers that return JSON (not redirect/HTML) ───────
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(401);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write(
                        "{\"timestamp\":\"" + java.time.Instant.now() + "\"," +
                        "\"status\":401," +
                        "\"code\":\"AUTHENTICATION_REQUIRED\"," +
                        "\"message\":\"Authentication is required to access this resource\"," +
                        "\"path\":\"" + req.getRequestURI() + "\"}");
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(403);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write(
                        "{\"timestamp\":\"" + java.time.Instant.now() + "\"," +
                        "\"status\":403," +
                        "\"code\":\"ACCESS_DENIED\"," +
                        "\"message\":\"You do not have permission to perform this action\"," +
                        "\"path\":\"" + req.getRequestURI() + "\"}");
                })
            );

        return http.build();
    }

    // ── Beans ─────────────────────────────────────────────────────────────────

    /**
     * BCrypt with work factor 12 — strong enough for production without being
     * excessively slow (< 300 ms on modern hardware).
     * Never use MD5, SHA-1, or plain SHA-256 for passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // In production restrict to your actual frontend domain
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(
                List.of("X-Correlation-Id", "X-Rate-Limit-Remaining", "Retry-After"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
