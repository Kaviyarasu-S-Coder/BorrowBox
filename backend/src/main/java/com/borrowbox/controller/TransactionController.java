package com.borrowbox.controller;

import com.borrowbox.dto.request.ConfirmCodeDto;
import com.borrowbox.dto.request.RecordConditionDto;
import com.borrowbox.dto.response.ApiResponse;
import com.borrowbox.dto.response.TransactionConditionResponse;
import com.borrowbox.dto.response.TransactionResponse;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Borrow Transactions", description = "Endpoints for handover verification, return tracking, and condition snapshots")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction details", description = "Returns full details, verification status, and condition proofs.")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id
    ) {
        TransactionResponse response = transactionService.getTransactionById(currentUser, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/pickup")
    @Operation(summary = "Confirm item pickup", description = "Validates 6-digit pickup verification code and changes status to BORROWED.")
    public ResponseEntity<ApiResponse<TransactionResponse>> confirmPickup(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody ConfirmCodeDto dto
    ) {
        TransactionResponse response = transactionService.confirmPickup(currentUser, id, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Item pickup successfully confirmed"));
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "Confirm item return", description = "Validates 6-digit return verification code and completes transaction.")
    public ResponseEntity<ApiResponse<TransactionResponse>> confirmReturn(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody ConfirmCodeDto dto
    ) {
        TransactionResponse response = transactionService.confirmReturn(currentUser, id, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Item return successfully confirmed"));
    }

    @PostMapping("/{id}/condition")
    @Operation(summary = "Record condition snapshot", description = "Logs photos and condition assessment during pickup or return.")
    public ResponseEntity<ApiResponse<TransactionConditionResponse>> recordCondition(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody RecordConditionDto dto
    ) {
        TransactionConditionResponse response = transactionService.recordCondition(currentUser, id, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Condition proof recorded successfully"));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's transactions", description = "Returns paginated list of transactions.")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getMyTransactions(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<TransactionResponse> page = transactionService.getMyTransactions(currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }
}
