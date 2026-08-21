package com.ecommerce.identity.application.dto;

import com.ecommerce.identity.domain.model.AddressType;

import java.util.UUID;

public record AddressResponse(
        UUID id,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String country,
        AddressType addressType,
        boolean defaultAddress,
        String label
) {}
