package com.borrowbox.controller;

import com.borrowbox.dto.response.ApiResponse;
import com.borrowbox.dto.response.FavoriteStatusResponse;
import com.borrowbox.dto.response.ItemResponse;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites & Watchlist", description = "Endpoints for managing saved items and user watchlists")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{itemId}/toggle")
    @Operation(summary = "Toggle favorite status", description = "Adds or removes an item from the user's personal favorites.")
    public ResponseEntity<ApiResponse<FavoriteStatusResponse>> toggleFavorite(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long itemId
    ) {
        FavoriteStatusResponse response = favoriteService.toggleFavorite(currentUser, itemId);
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    @GetMapping
    @Operation(summary = "Get user's favorites", description = "Retrieves paginated list of items saved in user's watchlist.")
    public ResponseEntity<ApiResponse<Page<ItemResponse>>> getUserFavorites(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ItemResponse> favorites = favoriteService.getUserFavorites(currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.success(favorites));
    }

    @GetMapping("/{itemId}/status")
    @Operation(summary = "Check favorite status", description = "Checks whether an item is favorited by the current user.")
    public ResponseEntity<ApiResponse<FavoriteStatusResponse>> getFavoriteStatus(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long itemId
    ) {
        FavoriteStatusResponse response = favoriteService.getFavoriteStatus(currentUser, itemId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
