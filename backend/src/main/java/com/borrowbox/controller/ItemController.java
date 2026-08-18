package com.borrowbox.controller;

import com.borrowbox.dto.request.CreateItemRequest;
import com.borrowbox.dto.request.UpdateItemRequest;
import com.borrowbox.dto.response.ApiResponse;
import com.borrowbox.dto.response.ItemResponse;
import com.borrowbox.dto.response.ItemSummaryResponse;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Item Listing", description = "Endpoints for creating, managing, and viewing item listings")
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @Operation(summary = "Create a new item listing", description = "Lists an item for borrowing with conditions, deposits, and rules.")
    public ResponseEntity<ApiResponse<ItemResponse>> createItem(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateItemRequest request
    ) {
        ItemResponse response = itemService.createItem(currentUser, request);
        return new ResponseEntity<>(ApiResponse.success(response, "Item listed successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item details", description = "Returns full details of an item including owner profile and condition photographs.")
    public ResponseEntity<ApiResponse<ItemResponse>> getItemById(@PathVariable Long id) {
        ItemResponse response = itemService.getItemById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an item", description = "Updates item specifications, rules, or condition (owner only).")
    public ResponseEntity<ApiResponse<ItemResponse>> updateItem(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody UpdateItemRequest request
    ) {
        ItemResponse response = itemService.updateItem(currentUser, id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Item updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete / Deactivate an item", description = "Deactivates an item (blocked if active bookings exist).")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id
    ) {
        itemService.deleteItem(currentUser, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Item deactivated successfully"));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's listed items", description = "Returns all items listed by the authenticated owner.")
    public ResponseEntity<ApiResponse<Page<ItemSummaryResponse>>> getMyItems(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ItemSummaryResponse> items = itemService.getMyItems(currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload item image", description = "Uploads a photo for an item.")
    public ResponseEntity<ApiResponse<ItemResponse>> uploadImage(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isPrimary", defaultValue = "false") boolean isPrimary
    ) {
        ItemResponse response = itemService.uploadItemImage(currentUser, id, file, isPrimary);
        return ResponseEntity.ok(ApiResponse.success(response, "Image uploaded successfully"));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @Operation(summary = "Delete item image", description = "Deletes a specific photo from an item.")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @PathVariable Long imageId
    ) {
        itemService.deleteItemImage(currentUser, id, imageId);
        return ResponseEntity.ok(ApiResponse.success(null, "Image deleted successfully"));
    }
}
