package com.ecommerce.identity.api;

import com.ecommerce.identity.application.dto.*;
import com.ecommerce.identity.application.service.CustomerService;
import com.ecommerce.shared.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Customer self-service endpoints — authenticated customer only.
 */
@Tag(name = "Customer", description = "Customer profile and address management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Get current customer profile")
    @GetMapping("/me")
    public ApiResponse<CustomerResponse> getProfile(@AuthenticationPrincipal String customerId) {
        return ApiResponse.success(customerService.getCustomer(UUID.fromString(customerId)));
    }

    @Operation(summary = "Update current customer profile")
    @PutMapping("/me")
    public ApiResponse<CustomerResponse> updateProfile(
            @AuthenticationPrincipal String customerId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success(customerService.updateProfile(UUID.fromString(customerId), request));
    }

    @Operation(summary = "Change current customer password")
    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(
            @AuthenticationPrincipal String customerId,
            @Valid @RequestBody ChangePasswordRequest request) {
        customerService.changePassword(UUID.fromString(customerId), request);
    }

    // ------------------------------------------------------------------ addresses

    @Operation(summary = "List all saved addresses")
    @GetMapping("/me/addresses")
    public ApiResponse<List<AddressResponse>> getAddresses(@AuthenticationPrincipal String customerId) {
        return ApiResponse.success(customerService.getAddresses(UUID.fromString(customerId)));
    }

    @Operation(summary = "Add a new address")
    @PostMapping("/me/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AddressResponse> addAddress(
            @AuthenticationPrincipal String customerId,
            @Valid @RequestBody AddressRequest request) {
        return ApiResponse.success(customerService.addAddress(UUID.fromString(customerId), request));
    }

    @Operation(summary = "Update an address")
    @PutMapping("/me/addresses/{addressId}")
    public ApiResponse<AddressResponse> updateAddress(
            @AuthenticationPrincipal String customerId,
            @PathVariable UUID addressId,
            @Valid @RequestBody AddressRequest request) {
        return ApiResponse.success(
                customerService.updateAddress(UUID.fromString(customerId), addressId, request));
    }

    @Operation(summary = "Set an address as default")
    @PatchMapping("/me/addresses/{addressId}/default")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void setDefault(
            @AuthenticationPrincipal String customerId,
            @PathVariable UUID addressId) {
        customerService.setDefaultAddress(UUID.fromString(customerId), addressId);
    }

    @Operation(summary = "Remove an address")
    @DeleteMapping("/me/addresses/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAddress(
            @AuthenticationPrincipal String customerId,
            @PathVariable UUID addressId) {
        customerService.removeAddress(UUID.fromString(customerId), addressId);
    }
}
