package com.ecommerce.identity.application.dto;

import com.ecommerce.shared.security.AppRole;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for an admin role-change operation.
 * {@code role} must be one of the constants in {@link AppRole}.
 */
public record ChangeRoleRequest(
        @NotBlank String role
) {}
