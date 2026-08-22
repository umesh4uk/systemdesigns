package com.ecommerce.payment.api;

import com.ecommerce.payment.application.dto.InitiatePaymentRequest;
import com.ecommerce.payment.application.dto.PaymentResponse;
import com.ecommerce.payment.application.service.PaymentService;
import com.ecommerce.shared.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@Tag(name = "Payments", description = "Payment processing")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Initiate payment for an order")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PaymentResponse> initiatePayment(
            @AuthenticationPrincipal String customerId,
            @Valid @RequestBody InitiatePaymentRequest request) {
        return ApiResponse.success(
                paymentService.initiatePayment(UUID.fromString(customerId), request));
    }

    @Operation(summary = "Get payment by ID")
    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        return ApiResponse.success(paymentService.getPayment(paymentId));
    }

    @Operation(summary = "Get payment by order ID")
    @GetMapping("/order/{orderId}")
    public ApiResponse<PaymentResponse> getPaymentByOrder(@PathVariable UUID orderId) {
        return ApiResponse.success(paymentService.getPaymentByOrder(orderId));
    }

    @Operation(summary = "Process refund (admin)")
    @PostMapping("/{paymentId}/refund")
    public ApiResponse<PaymentResponse> refund(
            @PathVariable UUID paymentId,
            @RequestParam java.math.BigDecimal amount,
            @RequestParam(required = false, defaultValue = "Customer request") String reason) {
        return ApiResponse.success(paymentService.processRefund(paymentId, amount, reason));
    }

    @Operation(summary = "Capture an authorized payment (called after order confirmation)")
    @PostMapping("/{paymentId}/capture")
    public ApiResponse<PaymentResponse> capture(@PathVariable UUID paymentId) {
        return ApiResponse.success(paymentService.capturePayment(paymentId));
    }
}
