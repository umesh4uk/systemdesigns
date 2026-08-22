package com.ecommerce.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test: verifies the full Spring application context loads successfully
 * with real Postgres, Redis, and Kafka provided by Testcontainers.
 *
 * <p>This test deliberately does NOT make HTTP calls — it only confirms that:
 * <ul>
 *   <li>All beans are wired correctly (no missing dependencies, no circular refs).</li>
 *   <li>Flyway migrations run without errors against the test schema.</li>
 *   <li>JPA schema validation passes ({@code spring.jpa.hibernate.ddl-auto=validate}).</li>
 *   <li>Redis and Kafka connections are established on startup.</li>
 * </ul>
 *
 * <p>This is intentionally fast and narrow — it is the canary that catches
 * config/wiring regressions before any other test runs.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,  // no HTTP server needed for context load
    properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.mail.host=localhost",
        "spring.mail.port=3025",   // non-existent — mail sending is async, won't block context load
        "app.jwt.secret=dGVzdFNlY3JldEtleVRoYXRJc0F0TGVhc3QzMkNoYXJhY3RlcnNMb25n"
    }
)
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
class ApplicationContextSmokeTest {

    @Autowired
    ApplicationContext context;

    @Test
    void context_loads_without_errors() {
        assertThat(context).isNotNull();
    }

    @Test
    void all_bounded_context_services_are_wired() {
        // Identity
        assertThat(context.containsBean("authService")).isTrue();
        assertThat(context.containsBean("customerService")).isTrue();

        // Catalog
        assertThat(context.containsBean("productService")).isTrue();
        assertThat(context.containsBean("categoryService")).isTrue();

        // Inventory
        assertThat(context.containsBean("inventoryService")).isTrue();

        // Pricing
        assertThat(context.containsBean("pricingService")).isTrue();

        // Promotion
        assertThat(context.containsBean("couponService")).isTrue();

        // Cart
        assertThat(context.containsBean("cartService")).isTrue();

        // Order
        assertThat(context.containsBean("orderService")).isTrue();

        // Payment
        assertThat(context.containsBean("paymentService")).isTrue();

        // Notification listeners
        assertThat(context.containsBean("orderEventListener")).isTrue();
        assertThat(context.containsBean("customerEventListener")).isTrue();
        assertThat(context.containsBean("notificationPaymentEventListener")).isTrue();
        assertThat(context.containsBean("orderPaymentEventListener")).isTrue();

        // Cross-cutting
        assertThat(context.containsBean("dashboardService")).isTrue();
    }

    @Test
    void all_rest_controllers_are_wired() {
        assertThat(context.containsBean("authController")).isTrue();
        assertThat(context.containsBean("customerController")).isTrue();
        assertThat(context.containsBean("productController")).isTrue();
        assertThat(context.containsBean("categoryController")).isTrue();
        assertThat(context.containsBean("cartController")).isTrue();
        assertThat(context.containsBean("orderController")).isTrue();
        assertThat(context.containsBean("paymentController")).isTrue();
        assertThat(context.containsBean("couponController")).isTrue();
        assertThat(context.containsBean("inventoryController")).isTrue();
        assertThat(context.containsBean("adminProductController")).isTrue();
        assertThat(context.containsBean("adminOrderController")).isTrue();
        assertThat(context.containsBean("adminCustomerController")).isTrue();
        assertThat(context.containsBean("adminPricingController")).isTrue();
        assertThat(context.containsBean("dashboardController")).isTrue();
    }
}
