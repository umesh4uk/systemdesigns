package com.ecommerce.pricing.api;

import com.ecommerce.pricing.application.dto.CreatePriceRuleRequest;
import com.ecommerce.pricing.application.dto.EffectivePriceResponse;
import com.ecommerce.pricing.application.dto.PriceRuleResponse;
import com.ecommerce.pricing.application.service.PricingService;
import com.ecommerce.shared.api.response.ApiResponse;
import com.ecommerce.shared.domain.valueobject.Money;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Admin price-rule management endpoints.
 *
 * <p>Design: price rules are time-bounded discount overrides for a product.
 * A PERCENTAGE rule of 20% on a $100 product makes it $80.
 * A FIXED rule of $15 on a $100 product makes it $85.
 * The most-discounted active rule wins at checkout.
 */
@Tag(name = "Admin - Pricing", description = "Price rule management")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/pricing")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPricingController {

    private final PricingService pricingService;

    // ── price rules ───────────────────────────────────────────────────────────

    @Operation(summary = "Create a price rule for a product")
    @PostMapping("/rules")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PriceRuleResponse> createRule(
            @Valid @RequestBody CreatePriceRuleRequest request) {
        return ApiResponse.success(pricingService.createRule(request));
    }

    @Operation(summary = "Get a price rule by ID")
    @GetMapping("/rules/{ruleId}")
    public ApiResponse<PriceRuleResponse> getRule(@PathVariable UUID ruleId) {
        return ApiResponse.success(pricingService.getRule(ruleId));
    }

    @Operation(summary = "List all price rules for a product")
    @GetMapping("/rules")
    public ApiResponse<List<PriceRuleResponse>> listRules(
            @RequestParam @NotNull UUID productId) {
        return ApiResponse.success(pricingService.getRulesForProduct(productId));
    }

    @Operation(summary = "Deactivate a price rule")
    @DeleteMapping("/rules/{ruleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateRule(@PathVariable UUID ruleId) {
        pricingService.deactivateRule(ruleId);
    }

    // ── price preview ─────────────────────────────────────────────────────────

    @Operation(summary = "Preview effective price for a product given its base price")
    @GetMapping("/preview")
    public ApiResponse<EffectivePriceResponse> previewPrice(
            @RequestParam @NotNull UUID productId,
            @RequestParam @NotNull @DecimalMin("0.00") BigDecimal basePrice,
            @RequestParam(defaultValue = "USD") @NotBlank String currency) {
        return ApiResponse.success(
                pricingService.getEffectivePriceDetails(productId, Money.of(basePrice, currency)));
    }
}
