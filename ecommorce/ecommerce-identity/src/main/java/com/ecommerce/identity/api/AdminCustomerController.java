package com.ecommerce.identity.api;

import com.ecommerce.identity.application.dto.CustomerResponse;
import com.ecommerce.identity.application.service.CustomerService;
import com.ecommerce.shared.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin-only customer management endpoints.
 */
@Tag(name = "Admin - Customers", description = "Admin customer management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/customers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Get a customer by ID")
    @GetMapping("/{customerId}")
    public ApiResponse<CustomerResponse> getCustomer(@PathVariable UUID customerId) {
        return ApiResponse.success(customerService.getCustomer(customerId));
    }

    @Operation(summary = "Suspend a customer account")
    @PostMapping("/{customerId}/suspend")
    public ApiResponse<CustomerResponse> suspend(@PathVariable UUID customerId) {
        return ApiResponse.success(customerService.suspendCustomer(customerId));
    }

    @Operation(summary = "Activate a customer account")
    @PostMapping("/{customerId}/activate")
    public ApiResponse<CustomerResponse> activate(@PathVariable UUID customerId) {
        return ApiResponse.success(customerService.activateCustomer(customerId));
    }
}
