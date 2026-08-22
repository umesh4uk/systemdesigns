package com.ecommerce.cart.domain.model;

import com.ecommerce.shared.domain.valueobject.Money;
import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the Cart domain model.
 * No Spring context — pure domain logic.
 */
class CartTest {

    private Cart cart;
    private static final UUID CUSTOMER_ID  = UUID.randomUUID();
    private static final UUID PRODUCT_A    = UUID.randomUUID();
    private static final UUID PRODUCT_B    = UUID.randomUUID();

    private static CartItem item(UUID productId, int qty, String price) {
        return new CartItem(productId, "SKU-" + productId.toString().substring(0, 4),
                "Product " + productId.toString().substring(0, 4),
                qty, Money.of(price, "USD"), null);
    }

    @BeforeEach
    void setUp() {
        cart = new Cart(CUSTOMER_ID, "USD");
    }

    // ── addItem ───────────────────────────────────────────────────────────────

    @Test
    void should_add_item_to_empty_cart() {
        cart.addItem(item(PRODUCT_A, 2, "10.00"));
        assertThat(cart.getItemList()).hasSize(1);
        assertThat(cart.getItemList().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void should_accumulate_quantity_when_same_product_added_twice() {
        cart.addItem(item(PRODUCT_A, 2, "10.00"));
        cart.addItem(item(PRODUCT_A, 3, "10.00"));
        assertThat(cart.getItemList()).hasSize(1);
        assertThat(cart.getItemList().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void should_add_different_products_as_separate_items() {
        cart.addItem(item(PRODUCT_A, 1, "10.00"));
        cart.addItem(item(PRODUCT_B, 1, "20.00"));
        assertThat(cart.getItemList()).hasSize(2);
    }

    @Test
    void should_reject_item_with_zero_quantity() {
        assertThatThrownBy(() -> item(PRODUCT_A, 0, "10.00"))
                .isInstanceOf(com.ecommerce.shared.exception.DomainException.class)
                .hasMessageContaining("positive");
    }

    @Test
    void should_reject_item_with_negative_quantity() {
        assertThatThrownBy(() -> item(PRODUCT_A, -1, "10.00"))
                .isInstanceOf(com.ecommerce.shared.exception.DomainException.class);
    }

    @Test
    void should_throw_when_cart_exceeds_50_items() {
        for (int i = 0; i < 50; i++) {
            cart.addItem(item(UUID.randomUUID(), 1, "5.00"));
        }
        assertThatThrownBy(() -> cart.addItem(item(UUID.randomUUID(), 1, "5.00")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("50");
    }

    // ── removeItem ────────────────────────────────────────────────────────────

    @Test
    void should_remove_existing_item() {
        cart.addItem(item(PRODUCT_A, 1, "10.00"));
        cart.removeItem(PRODUCT_A);
        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void should_throw_when_removing_non_existent_item() {
        assertThatThrownBy(() -> cart.removeItem(PRODUCT_A))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── updateItemQuantity ────────────────────────────────────────────────────

    @Test
    void should_update_existing_item_quantity() {
        cart.addItem(item(PRODUCT_A, 1, "10.00"));
        cart.updateItemQuantity(PRODUCT_A, 5);
        assertThat(cart.getItemList().get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void should_throw_when_updating_quantity_to_zero() {
        cart.addItem(item(PRODUCT_A, 1, "10.00"));
        assertThatThrownBy(() -> cart.updateItemQuantity(PRODUCT_A, 0))
                .isInstanceOf(com.ecommerce.shared.exception.DomainException.class);
    }

    // ── totals ────────────────────────────────────────────────────────────────

    @Test
    void should_compute_subtotal_correctly() {
        cart.addItem(item(PRODUCT_A, 2, "10.00"));  // $20
        cart.addItem(item(PRODUCT_B, 3, "5.00"));   // $15
        assertThat(cart.getSubtotal().getAmount()).isEqualByComparingTo("35.00");
    }

    @Test
    void should_return_zero_subtotal_for_empty_cart() {
        assertThat(cart.getSubtotal().getAmount()).isEqualByComparingTo("0.00");
    }

    @Test
    void should_subtract_coupon_from_total() {
        cart.addItem(item(PRODUCT_A, 1, "100.00"));
        cart.applyCoupon("SAVE20", Money.of("20.00", "USD"));
        assertThat(cart.getTotal().getAmount()).isEqualByComparingTo("80.00");
    }

    @Test
    void should_return_subtotal_as_total_when_no_coupon() {
        cart.addItem(item(PRODUCT_A, 2, "25.00"));
        assertThat(cart.getTotal()).isEqualTo(cart.getSubtotal());
    }

    // ── coupon ────────────────────────────────────────────────────────────────

    @Test
    void should_apply_coupon() {
        cart.addItem(item(PRODUCT_A, 1, "100.00"));
        cart.applyCoupon("PROMO10", Money.of("10.00", "USD"));
        assertThat(cart.getAppliedCouponCode()).isEqualTo("PROMO10");
        assertThat(cart.getCouponDiscount().getAmount()).isEqualByComparingTo("10.00");
    }

    @Test
    void should_clear_coupon() {
        cart.addItem(item(PRODUCT_A, 1, "100.00"));
        cart.applyCoupon("PROMO10", Money.of("10.00", "USD"));
        cart.clearCoupon();
        assertThat(cart.getAppliedCouponCode()).isNull();
        assertThat(cart.getCouponDiscount()).isNull();
        assertThat(cart.getTotal()).isEqualTo(cart.getSubtotal());
    }

    @Test
    void should_clear_coupon_automatically_when_item_removed() {
        cart.addItem(item(PRODUCT_A, 1, "100.00"));
        cart.addItem(item(PRODUCT_B, 1, "50.00"));
        cart.applyCoupon("PROMO10", Money.of("10.00", "USD"));
        cart.removeItem(PRODUCT_A);
        assertThat(cart.getAppliedCouponCode()).isNull();
    }

    // ── clear ─────────────────────────────────────────────────────────────────

    @Test
    void should_clear_all_items_and_coupon() {
        cart.addItem(item(PRODUCT_A, 1, "10.00"));
        cart.addItem(item(PRODUCT_B, 2, "20.00"));
        cart.applyCoupon("PROMO", Money.of("5.00", "USD"));
        cart.clear();
        assertThat(cart.isEmpty()).isTrue();
        assertThat(cart.getAppliedCouponCode()).isNull();
    }

    // ── isEmpty ───────────────────────────────────────────────────────────────

    @Test
    void should_be_empty_initially() {
        assertThat(cart.isEmpty()).isTrue();
    }

    @Test
    void should_not_be_empty_after_adding_item() {
        cart.addItem(item(PRODUCT_A, 1, "5.00"));
        assertThat(cart.isEmpty()).isFalse();
    }
}
