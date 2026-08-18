package com.borrowbox.controller;

import com.borrowbox.dto.request.CreateRatingDto;
import com.borrowbox.dto.response.ApiResponse;
import com.borrowbox.dto.response.RatingResponse;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.RatingService;
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
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@Tag(name = "Ratings & Reviews", description = "Endpoints for submitting and viewing user reviews and reputation")
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    @Operation(summary = "Submit a rating & review", description = "Rates the counterpart after a transaction completes.")
    public ResponseEntity<ApiResponse<RatingResponse>> createRating(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody CreateRatingDto dto
    ) {
        RatingResponse response = ratingService.createRating(currentUser, dto);
        return new ResponseEntity<>(ApiResponse.success(response, "Rating submitted successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user reviews", description = "Returns public list of reviews received by a user.")
    public ResponseEntity<ApiResponse<Page<RatingResponse>>> getUserRatings(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<RatingResponse> ratings = ratingService.getUserRatings(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(ratings));
    }
}
