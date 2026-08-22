package com.ecommerce.app.api;

import com.ecommerce.app.TestContainersConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the auth and product public endpoints.
 * Uses MockMvc + Testcontainers for real DB/Redis/Kafka.
 *
 * <p>Test order is explicit so that the JWT obtained at login is reused.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "app.jwt.secret=dGVzdFNlY3JldEtleVRoYXRJc0F0TGVhc3QzMkNoYXJhY3RlcnNMb25n",
        "spring.mail.host=localhost",
        "spring.mail.port=3025"
    }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestContainersConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthApiIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // shared state across ordered tests
    private static String accessToken;

    // ── registration ──────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void should_register_new_customer() throws Exception {
        String body = """
                {
                  "email":     "test.user@example.com",
                  "password":  "SecurePass1!",
                  "firstName": "Test",
                  "lastName":  "User",
                  "phone":     "+12025551234"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test.user@example.com"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @Order(2)
    void should_reject_duplicate_registration() throws Exception {
        String body = """
                {
                  "email":     "test.user@example.com",
                  "password":  "SecurePass1!",
                  "firstName": "Test",
                  "lastName":  "User"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @Order(3)
    void should_reject_registration_with_invalid_email() throws Exception {
        String body = """
                {
                  "email":     "not-an-email",
                  "password":  "SecurePass1!",
                  "firstName": "Test",
                  "lastName":  "User"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    @Order(4)
    void should_reject_registration_with_short_password() throws Exception {
        String body = """
                {
                  "email":     "another@example.com",
                  "password":  "short",
                  "firstName": "Test",
                  "lastName":  "User"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnprocessableEntity());
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    @Order(5)
    void should_login_and_receive_tokens() throws Exception {
        String body = """
                {
                  "email":    "test.user@example.com",
                  "password": "SecurePass1!"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andReturn();

        JsonNode data = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data");
        accessToken = data.get("accessToken").asText();
        assertThat(accessToken).isNotBlank();
    }

    @Test
    @Order(6)
    void should_reject_login_with_wrong_password() throws Exception {
        String body = """
                {
                  "email":    "test.user@example.com",
                  "password": "WrongPassword!"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isUnauthorized());
    }

    // ── customer profile ──────────────────────────────────────────────────────

    @Test
    @Order(7)
    void should_return_current_customer_profile_with_valid_token() throws Exception {
        assertThat(accessToken).as("login must run first").isNotNull();

        mockMvc.perform(get("/api/v1/customers/me")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("test.user@example.com"))
                .andExpect(jsonPath("$.data.firstName").value("Test"));
    }

    @Test
    @Order(8)
    void should_return_401_when_no_token_provided_for_protected_endpoint() throws Exception {
        mockMvc.perform(get("/api/v1/customers/me"))
                .andExpect(status().isUnauthorized());
    }

    // ── public product endpoints ──────────────────────────────────────────────

    @Test
    @Order(9)
    void should_return_empty_product_list_without_auth() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.page").value(0));
    }

    @Test
    @Order(10)
    void should_return_category_tree_without_auth() throws Exception {
        // Seed data from V8 migration inserts Electronics, Clothing, etc.
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    // ── actuator health ───────────────────────────────────────────────────────

    @Test
    @Order(11)
    void actuator_health_should_be_up() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    // ── admin protection ──────────────────────────────────────────────────────

    @Test
    @Order(12)
    void admin_endpoint_should_return_403_for_customer_token() throws Exception {
        assertThat(accessToken).as("login must run first").isNotNull();

        // A customer JWT does not carry ROLE_ADMIN → 403
        mockMvc.perform(get("/api/v1/admin/dashboard/summary")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(13)
    void admin_endpoint_should_return_401_without_token() throws Exception {
        mockMvc.perform(get("/api/v1/admin/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }
}
