package com.ecommerce.identity.domain.model;

import com.ecommerce.identity.domain.event.CustomerRegisteredEvent;
import com.ecommerce.shared.domain.valueobject.Address;
import com.ecommerce.shared.exception.BusinessRuleException;
import com.ecommerce.shared.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CustomerTest {

    private static Customer validCustomer() {
        return Customer.register("john@example.com", "hashed_pw", "John", "Doe", "+1234567890");
    }

    @Test
    void should_register_customer_with_active_status() {
        Customer c = validCustomer();
        assertThat(c.getStatus()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(c.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void should_emit_registered_event_on_creation() {
        Customer c = validCustomer();
        assertThat(c.getDomainEvents())
                .hasSize(1)
                .first()
                .isInstanceOf(CustomerRegisteredEvent.class);
    }

    @Test
    void should_clear_domain_events() {
        Customer c = validCustomer();
        c.clearDomainEvents();
        assertThat(c.getDomainEvents()).isEmpty();
    }

    @Test
    void should_reject_invalid_email() {
        assertThatThrownBy(() ->
                Customer.register("not-an-email", "hash", "John", "Doe", null))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void should_suspend_active_customer() {
        Customer c = validCustomer();
        c.suspend();
        assertThat(c.getStatus()).isEqualTo(CustomerStatus.SUSPENDED);
        assertThat(c.isActive()).isFalse();
    }

    @Test
    void should_not_suspend_deactivated_customer() {
        Customer c = validCustomer();
        c.deactivate();
        assertThatThrownBy(c::suspend).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void should_add_address_and_mark_first_as_default() {
        Customer c = validCustomer();
        Address addr = Address.builder()
                .addressLine1("123 Main St")
                .city("Springfield")
                .postalCode("12345")
                .country("US")
                .build();

        c.addAddress(addr, AddressType.SHIPPING, false, "Home");

        List<CustomerAddress> addresses = c.getAddresses();
        assertThat(addresses).hasSize(1);
        assertThat(addresses.get(0).isDefaultAddress()).isTrue(); // first is auto-default
    }

    @Test
    void should_not_remove_default_address() {
        Customer c = validCustomer();
        Address addr = Address.builder()
                .addressLine1("123 Main St").city("Springfield")
                .postalCode("12345").country("US").build();
        CustomerAddress saved = c.addAddress(addr, AddressType.SHIPPING, true, "Home");

        assertThatThrownBy(() -> c.removeAddress(saved.getId()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("default");
    }
}
