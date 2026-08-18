package com.borrowbox.controller;

import com.borrowbox.dto.response.AdminUserResponse;
import com.borrowbox.dto.response.ApiResponse;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin User Moderation", description = "Endpoints for managing and moderating user accounts")
public class AdminUserController {

    private final AdminService adminService;

    @GetMapping
    @Operation(summary = "Admin: list users", description = "Paginated list of users with active status filtering.")
    public ResponseEntity<ApiResponse<Page<AdminUserResponse>>> getAllUsers(
            @AuthenticationPrincipal UserPrincipal adminUser,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AdminUserResponse> users = adminService.getAllUsers(adminUser, search, isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PutMapping("/{userId}/toggle-status")
    @Operation(summary = "Admin: toggle user active status", description = "Bans or unbans a user account.")
    public ResponseEntity<ApiResponse<AdminUserResponse>> toggleUserActiveStatus(
            @AuthenticationPrincipal UserPrincipal adminUser,
            @PathVariable Long userId
    ) {
        AdminUserResponse response = adminService.toggleUserActiveStatus(adminUser, userId);
        String msg = response.isActive() ? "User account activated." : "User account suspended.";
        return ResponseEntity.ok(ApiResponse.success(response, msg));
    }

    @PutMapping("/{userId}/toggle-verify")
    @Operation(summary = "Admin: toggle user verification", description = "Verifies or unverifies user identity badge.")
    public ResponseEntity<ApiResponse<AdminUserResponse>> toggleUserVerification(
            @AuthenticationPrincipal UserPrincipal adminUser,
            @PathVariable Long userId
    ) {
        AdminUserResponse response = adminService.toggleUserVerification(adminUser, userId);
        String msg = response.isVerified() ? "User verified successfully." : "User verification revoked.";
        return ResponseEntity.ok(ApiResponse.success(response, msg));
    }
}
