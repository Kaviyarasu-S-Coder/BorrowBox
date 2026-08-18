package com.borrowbox.controller;

import com.borrowbox.dto.response.ApiResponse;
import com.borrowbox.service.ScheduledJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/jobs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Scheduled Jobs", description = "Endpoints for triggering and verifying background maintenance jobs")
public class AdminJobController {

    private final ScheduledJobService scheduledJobService;

    @PostMapping("/trigger-overdue")
    @Operation(summary = "Admin: Trigger Overdue Job", description = "Manually triggers the overdue transaction processor.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerOverdueJob() {
        int count = scheduledJobService.checkAndProcessOverdueTransactions();
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("processedCount", count, "job", "OVERDUE_DETECTION"),
                "Overdue detection job executed successfully."
        ));
    }

    @PostMapping("/trigger-reminders")
    @Operation(summary = "Admin: Trigger Reminders Job", description = "Manually triggers the upcoming return reminder dispatcher.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerRemindersJob() {
        int count = scheduledJobService.sendUpcomingReminders();
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("dispatchedCount", count, "job", "UPCOMING_REMINDERS"),
                "Upcoming reminders job executed successfully."
        ));
    }

    @PostMapping("/trigger-expired-requests")
    @Operation(summary = "Admin: Trigger Request Expiration Job", description = "Manually triggers the pending request auto-expiration scanner.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerExpiredRequestsJob() {
        int count = scheduledJobService.expirePendingRequests();
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("expiredCount", count, "job", "REQUEST_EXPIRATION"),
                "Pending requests auto-expiration job executed successfully."
        ));
    }
}
