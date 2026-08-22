package com.ecommerce.app.dashboard;

import com.ecommerce.shared.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin dashboard endpoints.
 *
 * <p>Accessible at {@code GET /api/v1/admin/dashboard/summary}.
 * Results are cached for 60 seconds — see {@link DashboardCacheConfig}.
 */
@Tag(name = "Admin - Dashboard", description = "Platform metrics and reporting")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(
        summary     = "Get dashboard summary",
        description = "Returns aggregated metrics: customer counts, order volumes, "
                    + "revenue totals, and low-stock alerts. Cached for 60 seconds."
    )
    @GetMapping("/summary")
    public ApiResponse<DashboardSummary> getSummary() {
        return ApiResponse.success(dashboardService.getSummary());
    }
}
