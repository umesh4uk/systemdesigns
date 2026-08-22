package com.ecommerce.catalog.domain.model;

import com.ecommerce.catalog.domain.event.ProductCreatedEvent;
import com.ecommerce.shared.domain.valueobject.Money;
import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class ProductTest {

    private static Product validProduct() {
        return Product.create("PROD-001", "Test Product", "Description",
                "Short", null, "Acme", Money.of("99.99", "USD"), 500);
    }

    @Test
    void should_create_product_in_draft_status() {
        Product p = validProduct();
        assertThat(p.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(p.getSku()).isEqualTo("PROD-001");
    }

    @Test
    void should_emit_created_event() {
        Product p = validProduct();
        assertThat(p.getDomainEvents())
                .hasSize(1)
                .first().isInstanceOf(ProductCreatedEvent.class);
    }

    @Test
    void should_reject_invalid_sku_format() {
        assertThatThrownBy(() ->
                Product.create("bad sku!", "name", null, null, null, null,
                        Money.of("10", "USD"), null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Invalid SKU");
    }

    @Test
    void should_reject_blank_name() {
        assertThatThrownBy(() ->
                Product.create("VALID-SKU", "  ", null, null, null, null,
                        Money.of("10", "USD"), null))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void should_not_publish_without_images() {
        Product p = validProduct();
        assertThatThrownBy(p::publish)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("image");
    }

    @Test
    void should_publish_after_adding_image() {
        Product p = validProduct();
        p.addImage("http://example.com/img.jpg", "alt", 0);
        p.publish();
        assertThat(p.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(p.isAvailable()).isTrue();
    }

    @Test
    void should_auto_mark_first_image_as_primary() {
        Product p = validProduct();
        p.addImage("http://example.com/img1.jpg", "alt1", 0);
        p.addImage("http://example.com/img2.jpg", "alt2", 1);
        assertThat(p.getImages().get(0).isPrimary()).isTrue();
        assertThat(p.getImages().get(1).isPrimary()).isFalse();
    }

    @Test
    void should_set_and_overwrite_attribute() {
        Product p = validProduct();
        p.setAttribute("color", "red");
        p.setAttribute("color", "blue");
        assertThat(p.getAttributes()).hasSize(1);
        assertThat(p.getAttributes().get(0).getValue()).isEqualTo("blue");
    }

    @Test
    void should_reject_negative_price() {
        assertThatThrownBy(() ->
                Product.create("PROD-002", "name", null, null, null, null,
                        Money.of(BigDecimal.valueOf(-1), "USD"), null))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void should_archive_active_product() {
        Product p = validProduct();
        p.addImage("http://example.com/img.jpg", "alt", 0);
        p.publish();
        p.archive();
        assertThat(p.getStatus()).isEqualTo(ProductStatus.ARCHIVED);
        assertThat(p.isAvailable()).isFalse();
    }
}
