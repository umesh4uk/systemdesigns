package com.ecommerce.order.domain.model;

import com.ecommerce.order.domain.event.OrderPlacedEvent;
import com.ecommerce.shared.domain.valueobject.Address;
import com.ecommerce.shared.domain.valueobject.Money;
import com.ecommerce.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class OrderTest {

    private static Address address() {
        return Address.builder().addressLine1("1 Main St")
                .city("NYC").postalCode("10001").country("US").build();
    }

    private static Order validOrder() {
        var item = new Order.OrderItemData(UUID.randomUUID(), "PROD-001",
                "Widget", 2, Money.of("50.00", "USD"));
        return Order.place(UUID.randomUUID(), List.of(item), address(), address(),
                Money.of("0", "USD"), Money.of("5.00", "USD"), null, "USD");
    }

    @Test
    void should_place_order_in_created_status() {
        Order order = validOrder();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getOrderNumber()).startsWith("ORD-");
    }

    @Test
    void should_emit_placed_event() {
        Order order = validOrder();
        assertThat(order.getDomainEvents()).hasSize(1)
                .first().isInstanceOf(OrderPlacedEvent.class);
    }

    @Test
    void should_reject_empty_items() {
        assertThatThrownBy(() ->
                Order.place(UUID.randomUUID(), List.of(), address(), address(),
                        Money.of("0", "USD"), Money.of("0", "USD"), null, "USD"))
                .isInstanceOf(com.ecommerce.shared.exception.DomainException.class);
    }

    @Test
    void should_follow_valid_state_machine() {
        Order order = validOrder();
        order.markPaymentPending();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        order.confirm();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        order.startProcessing();
        order.ship("TRACK-123");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(order.getTrackingNumber()).isEqualTo("TRACK-123");
        order.deliver();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    void should_reject_invalid_transition() {
        Order order = validOrder();
        assertThatThrownBy(order::deliver)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cannot transition");
    }

    @Test
    void should_not_cancel_shipped_order() {
        Order order = validOrder();
        order.markPaymentPending();
        order.confirm();
        order.startProcessing();
        order.ship("TRACK-001");
        assertThatThrownBy(() -> order.cancel("changed mind"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("shipped");
    }

    @Test
    void should_capture_prices_at_placement() {
        var item = new Order.OrderItemData(UUID.randomUUID(), "PROD-001",
                "Widget", 3, Money.of("25.00", "USD"));
        Order order = Order.place(UUID.randomUUID(), List.of(item), address(), address(),
                Money.of("5.00", "USD"), Money.of("0", "USD"), null, "USD");
        // subtotal = 75, discount = 5, shipping = 0 → total = 70
        assertThat(order.getTotalMoney().getAmount()).isEqualByComparingTo("70.00");
    }
}
