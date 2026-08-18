package com.borrowbox.controller;

import com.borrowbox.dto.response.ApiResponse;
import com.borrowbox.dto.response.AvailabilityCheckResponse;
import com.borrowbox.dto.response.DateRangeResponse;
import com.borrowbox.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/items/{itemId}")
@RequiredArgsConstructor
@Tag(name = "Item Availability", description = "Endpoints for checking item availability and booked date ranges")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/availability")
    @Operation(summary = "Check item availability", description = "Validates if an item is available between given start and end dates and computes deposit & rental costs.")
    public ResponseEntity<ApiResponse<AvailabilityCheckResponse>> checkAvailability(
            @PathVariable Long itemId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        AvailabilityCheckResponse response = availabilityService.checkAvailability(itemId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/calendar")
    @Operation(summary = "Get booked date ranges", description = "Returns all booked date intervals for rendering on calendar picker.")
    public ResponseEntity<ApiResponse<List<DateRangeResponse>>> getBookedDateRanges(
            @PathVariable Long itemId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        List<DateRangeResponse> bookedRanges = availabilityService.getBookedDateRanges(itemId, year, month);
        return ResponseEntity.ok(ApiResponse.success(bookedRanges));
    }
}
