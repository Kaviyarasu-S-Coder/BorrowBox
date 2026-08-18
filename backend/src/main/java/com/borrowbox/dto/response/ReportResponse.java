package com.borrowbox.dto.response;

import com.borrowbox.entity.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {

    private Long id;

    private Long reportedById;
    private String reportedByName;

    private Long reportedUserId;
    private String reportedUserName;

    private Long reportedItemId;
    private String reportedItemTitle;

    private String reason;
    private String description;

    private ReportStatus status;
    private String adminNotes;
    private LocalDateTime resolvedAt;

    private LocalDateTime createdAt;
}
