package com.borrowbox.controller;

import com.borrowbox.dto.request.CreateDisputeDto;
import com.borrowbox.dto.request.ResolveDisputeDto;
import com.borrowbox.dto.response.ApiResponse;
import com.borrowbox.dto.response.DisputeResponse;
import com.borrowbox.entity.DisputeStatus;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.DisputeService;
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
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
@Tag(name = "Disputes", description = "Endpoints for managing transaction disputes and admin resolution")
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping
    @Operation(summary = "File a dispute", description = "Raises a formal dispute on an active or completed transaction.")
    public ResponseEntity<ApiResponse<DisputeResponse>> createDispute(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateDisputeDto dto
    ) {
        DisputeResponse response = disputeService.createDispute(currentUser, dto);
        return new ResponseEntity<>(ApiResponse.success(response, "Dispute submitted successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/{disputeId}")
    @Operation(summary = "Get dispute by ID", description = "Retrieves details of a specific dispute.")
    public ResponseEntity<ApiResponse<DisputeResponse>> getDisputeById(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long disputeId
    ) {
        DisputeResponse response = disputeService.getDisputeById(currentUser, disputeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my")
    @Operation(summary = "Get my disputes", description = "Returns disputes filed by the current user.")
    public ResponseEntity<ApiResponse<Page<DisputeResponse>>> getMyDisputes(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<DisputeResponse> disputes = disputeService.getMyDisputes(currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.success(disputes));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: list all disputes", description = "Returns all disputes filtered by optional status.")
    public ResponseEntity<ApiResponse<Page<DisputeResponse>>> getAllDisputes(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) DisputeStatus status,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<DisputeResponse> disputes = disputeService.getAllDisputes(currentUser, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(disputes));
    }

    @PutMapping("/admin/{disputeId}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin: resolve dispute", description = "Applies admin decision and resolution to a dispute.")
    public ResponseEntity<ApiResponse<DisputeResponse>> resolveDispute(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long disputeId,
            @Valid @RequestBody ResolveDisputeDto dto
    ) {
        DisputeResponse response = disputeService.resolveDispute(currentUser, disputeId, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Dispute resolved successfully"));
    }
}
