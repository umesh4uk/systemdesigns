package com.ecommerce.identity.application.dto;

import com.ecommerce.identity.domain.model.CustomerStatus;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String phone,
        CustomerStatus status,
        Instant createdAt
) {}
