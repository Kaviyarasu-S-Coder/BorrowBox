package com.borrowbox.controller;

import com.borrowbox.dto.response.AdminStatsResponse;
import com.borrowbox.dto.response.ApiResponse;
import com.borrowbox.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Analytics & Dashboard", description = "Platform statistics and summary metrics for administrators")
public class AdminDashboardController {

    private final AdminService adminService;

    @GetMapping("/stats")
    @Operation(summary = "Get Platform Metrics", description = "Aggregates total users, items, transactions, deposits, and disputes.")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getPlatformStats() {
        AdminStatsResponse stats = adminService.getPlatformStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
