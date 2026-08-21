package com.ecommerce.shared.domain.valueobject;

import com.ecommerce.shared.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class MoneyTest {

    @Test
    void should_create_money_with_valid_amount() {
        Money money = Money.of(new BigDecimal("19.99"), "USD");
        assertThat(money.getAmount()).isEqualByComparingTo("19.99");
        assertThat(money.getCurrencyCode()).isEqualTo("USD");
    }

    @Test
    void should_reject_negative_amount() {
        assertThatThrownBy(() -> Money.of(new BigDecimal("-1"), "USD"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("negative");
    }

    @Test
    void should_add_same_currency() {
        Money a = Money.of("10.00", "USD");
        Money b = Money.of("5.50", "USD");
        assertThat(a.add(b).getAmount()).isEqualByComparingTo("15.50");
    }

    @Test
    void should_reject_adding_different_currencies() {
        Money usd = Money.of("10.00", "USD");
        Money eur = Money.of("10.00", "EUR");
        assertThatThrownBy(() -> usd.add(eur))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Currency mismatch");
    }

    @Test
    void should_subtract_correctly() {
        Money a = Money.of("20.00", "USD");
        Money b = Money.of("7.50", "USD");
        assertThat(a.subtract(b).getAmount()).isEqualByComparingTo("12.50");
    }

    @Test
    void should_reject_subtraction_resulting_in_negative() {
        Money a = Money.of("5.00", "USD");
        Money b = Money.of("10.00", "USD");
        assertThatThrownBy(() -> a.subtract(b))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void should_apply_percent_discount() {
        Money price = Money.of("100.00", "USD");
        Money discounted = price.discountByPercent(new BigDecimal("25"));
        assertThat(discounted.getAmount()).isEqualByComparingTo("75.00");
    }

    @Test
    void should_multiply_by_quantity() {
        Money price = Money.of("12.50", "USD");
        assertThat(price.multiply(3).getAmount()).isEqualByComparingTo("37.50");
    }

    @Test
    void should_be_equal_for_same_value() {
        Money a = Money.of("10.00", "USD");
        Money b = Money.of("10.00", "USD");
        assertThat(a).isEqualTo(b);
    }

    @Test
    void should_scale_to_currency_fraction_digits() {
        Money money = Money.of(new BigDecimal("19.999"), "USD");
        assertThat(money.getAmount().scale()).isEqualTo(2);
    }
}
