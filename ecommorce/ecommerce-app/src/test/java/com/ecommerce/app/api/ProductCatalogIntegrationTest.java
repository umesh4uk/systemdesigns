package com.ecommerce.app.api;

import com.ecommerce.app.TestContainersConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
 * Integration tests for the product catalog admin operations and
 * customer-facing search/detail endpoints.
 *
 * <p>Uses the seeded admin account (V8 migration) to create and publish a product,
 * then verifies it appears in the public search and can be retrieved by ID.
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductCatalogIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String adminToken;
    private String productId;

    @BeforeAll
    void loginAsAdmin() throws Exception {
        // The V8 seed migration inserts admin@ecommerce.example.com / Admin@123
        String body = """
                {
                  "email":    "admin@ecommerce.example.com",
                  "password": "Admin@123"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode data = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data");
        adminToken = data.get("accessToken").asText();
        assertThat(adminToken).isNotBlank();
    }

    // ── admin product CRUD ────────────────────────────────────────────────────

    @Test
    @Order(1)
    void admin_should_create_product_in_draft() throws Exception {
        String body = """
                {
                  "sku":              "LAPTOP-TEST-001",
                  "name":             "Test Laptop Pro",
                  "description":      "A great test laptop.",
                  "shortDescription": "Best test laptop",
                  "brand":            "TestBrand",
                  "basePrice":        999.99,
                  "currency":         "USD",
                  "weightGrams":      2000
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/admin/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.sku").value("LAPTOP-TEST-001"))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andReturn();

        productId = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data").get("id").asText();

        assertThat(productId).isNotBlank();
    }

    @Test
    @Order(2)
    void draft_product_should_not_appear_in_public_search() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                .param("keyword", "Test Laptop Pro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @Order(3)
    void admin_should_add_image_to_product() throws Exception {
        mockMvc.perform(post("/api/v1/admin/products/{id}/images", productId)
                .header("Authorization", "Bearer " + adminToken)
                .param("url", "https://cdn.example.com/laptop.jpg")
                .param("altText", "Test laptop front view")
                .param("displayOrder", "0"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.images.length()").value(1))
                .andExpect(jsonPath("$.data.images[0].primary").value(true));
    }

    @Test
    @Order(4)
    void admin_should_publish_product() throws Exception {
        mockMvc.perform(post("/api/v1/admin/products/{id}/publish", productId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @Order(5)
    void published_product_should_appear_in_public_search() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                .param("keyword", "Test Laptop Pro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.content[0].sku").value("LAPTOP-TEST-001"));
    }

    @Test
    @Order(6)
    void should_get_product_by_id() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(productId))
                .andExpect(jsonPath("$.data.name").value("Test Laptop Pro"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.images.length()").value(1));
    }

    @Test
    @Order(7)
    void should_get_product_by_sku() throws Exception {
        mockMvc.perform(get("/api/v1/products/sku/LAPTOP-TEST-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sku").value("LAPTOP-TEST-001"));
    }

    @Test
    @Order(8)
    void should_return_404_for_unknown_product_id() throws Exception {
        mockMvc.perform(get("/api/v1/products/{id}", java.util.UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @Order(9)
    void admin_should_not_publish_without_images() throws Exception {
        // Create a new product (no images) and try to publish it
        String body = """
                {
                  "sku":       "NO-IMAGE-SKU",
                  "name":      "Image-less Product",
                  "basePrice": 10.00,
                  "currency":  "USD"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/admin/products")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        String noImageProductId = objectMapper
                .readTree(result.getResponse().getContentAsString())
                .get("data").get("id").asText();

        // Publishing without an image should be rejected
        mockMvc.perform(post("/api/v1/admin/products/{id}/publish", noImageProductId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("NO_PRODUCT_IMAGES"));
    }

    @Test
    @Order(10)
    void admin_should_archive_product() throws Exception {
        mockMvc.perform(post("/api/v1/admin/products/{id}/archive", productId)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ARCHIVED"));
    }

    @Test
    @Order(11)
    void archived_product_should_not_appear_in_public_search() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                .param("keyword", "Test Laptop Pro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    // ── search / filter ───────────────────────────────────────────────────────

    @Test
    @Order(12)
    void search_should_support_pagination() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                .param("page", "0")
                .param("size", "5")
                .param("sortBy", "name")
                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.page").value(0));
    }

    @Test
    @Order(13)
    void search_should_filter_by_price_range() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                .param("minPrice", "0")
                .param("maxPrice", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }
}
