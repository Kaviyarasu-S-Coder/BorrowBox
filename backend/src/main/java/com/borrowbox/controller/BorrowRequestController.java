package com.borrowbox.controller;

import com.borrowbox.dto.request.CancelBorrowRequestDto;
import com.borrowbox.dto.request.CreateBorrowRequestDto;
import com.borrowbox.dto.request.RespondBorrowRequestDto;
import com.borrowbox.dto.response.ApiResponse;
import com.borrowbox.dto.response.BorrowRequestResponse;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.BorrowRequestService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrow-requests")
@RequiredArgsConstructor
@Tag(name = "Borrow Requests", description = "Endpoints for initiating, approving, rejecting, and tracking borrow requests")
public class BorrowRequestController {

    private final BorrowRequestService borrowRequestService;

    @PostMapping
    @Operation(summary = "Submit a borrow request", description = "Sends a new borrow booking request to the item owner.")
    public ResponseEntity<ApiResponse<BorrowRequestResponse>> createBorrowRequest(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateBorrowRequestDto dto
    ) {
        BorrowRequestResponse response = borrowRequestService.createBorrowRequest(currentUser, dto);
        return new ResponseEntity<>(ApiResponse.success(response, "Borrow request submitted successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get request details", description = "Returns full details of a specific borrow request.")
    public ResponseEntity<ApiResponse<BorrowRequestResponse>> getBorrowRequestById(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id
    ) {
        BorrowRequestResponse response = borrowRequestService.getBorrowRequestById(currentUser, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/respond")
    @Operation(summary = "Respond to borrow request (Owner)", description = "Accepts or rejects a borrow request.")
    public ResponseEntity<ApiResponse<BorrowRequestResponse>> respondToBorrowRequest(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody RespondBorrowRequestDto dto
    ) {
        BorrowRequestResponse response = borrowRequestService.respondToBorrowRequest(currentUser, id, dto);
        String msg = Boolean.TRUE.equals(dto.getAccept()) ? "Borrow request accepted" : "Borrow request rejected";
        return ResponseEntity.ok(ApiResponse.success(response, msg));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel borrow request (Borrower)", description = "Cancels a pending borrow request.")
    public ResponseEntity<ApiResponse<BorrowRequestResponse>> cancelBorrowRequest(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody CancelBorrowRequestDto dto
    ) {
        BorrowRequestResponse response = borrowRequestService.cancelBorrowRequest(currentUser, id, dto);
        return ResponseEntity.ok(ApiResponse.success(response, "Borrow request cancelled successfully"));
    }

    @GetMapping("/sent")
    @Operation(summary = "Get my sent requests", description = "Returns paginated list of borrow requests sent by the current user.")
    public ResponseEntity<ApiResponse<Page<BorrowRequestResponse>>> getMySentRequests(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<BorrowRequestResponse> requests = borrowRequestService.getMySentRequests(currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }

    @GetMapping("/received")
    @Operation(summary = "Get my received requests", description = "Returns paginated list of incoming borrow requests on owner's items.")
    public ResponseEntity<ApiResponse<Page<BorrowRequestResponse>>> getMyReceivedRequests(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<BorrowRequestResponse> requests = borrowRequestService.getMyReceivedRequests(currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.success(requests));
    }
}
