package com.borrowbox.controller;

import com.borrowbox.dto.request.CreateReportDto;
import com.borrowbox.dto.request.ResolveReportDto;
import com.borrowbox.dto.response.ApiResponse;
import com.borrowbox.dto.response.ReportResponse;
import com.borrowbox.entity.ReportStatus;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports & Moderation", description = "Endpoints for reporting inappropriate content and admin moderation")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "Submit a report", description = "Flags a suspicious user or inappropriate item listing.")
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateReportDto dto
    ) {
        ReportResponse response = reportService.createReport(currentUser, dto);
        return new ResponseEntity<>(ApiResponse.success(response, "Report submitted successfully. Our team will review it."), HttpStatus.CREATED);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: list reports", description = "Returns reports submitted by users with optional status filter.")
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> getAllReports(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ReportResponse> reports = reportService.getAllReports(currentUser, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @PutMapping("/admin/{reportId}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: resolve report", description = "Applies moderation action (ban user, deactivate item) on a report.")
    public ResponseEntity<ApiResponse<ReportResponse>> resolveReport(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long reportId,
            @Valid @RequestBody ResolveReportDto dto
    ) {
        ReportResponse response = reportService.resolveReport(currentUser, reportId, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Report resolved successfully"));
    }
}
