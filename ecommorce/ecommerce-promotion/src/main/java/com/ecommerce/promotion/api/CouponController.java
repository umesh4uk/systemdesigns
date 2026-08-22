package com.ecommerce.promotion.api;

import com.ecommerce.promotion.application.dto.CouponRequest;
import com.ecommerce.promotion.application.dto.CouponResponse;
import com.ecommerce.promotion.application.service.CouponService;
import com.ecommerce.shared.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Coupons", description = "Coupon management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "Validate and get coupon info")
    @GetMapping("/{code}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CouponResponse> getCoupon(@PathVariable String code) {
        return ApiResponse.success(couponService.getCoupon(code));
    }

    @Operation(summary = "Create coupon (admin)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CouponResponse> create(@Valid @RequestBody CouponRequest request) {
        return ApiResponse.success(couponService.createCoupon(request));
    }

    @Operation(summary = "Deactivate coupon (admin)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deactivate(@PathVariable UUID id) {
        couponService.deactivateCoupon(id);
    }
}
