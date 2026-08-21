package com.ecommerce.shared.domain.valueobject;

import com.ecommerce.shared.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class EmailAddressTest {

    @Test
    void should_create_valid_email() {
        EmailAddress email = EmailAddress.of("User@Example.COM");
        assertThat(email.getValue()).isEqualTo("user@example.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {"notanemail", "@domain.com", "user@", "user@domain", ""})
    void should_reject_invalid_emails(String invalid) {
        assertThatThrownBy(() -> EmailAddress.of(invalid))
                .isInstanceOf(DomainException.class);
    }

    @Test
    void should_be_equal_for_same_address() {
        assertThat(EmailAddress.of("a@b.com")).isEqualTo(EmailAddress.of("A@B.COM"));
    }

    @Test
    void should_reject_null() {
        assertThatThrownBy(() -> EmailAddress.of(null))
                .isInstanceOf(NullPointerException.class);
    }
}
