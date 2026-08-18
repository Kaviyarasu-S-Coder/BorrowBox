package com.borrowbox.dto.request;

import com.borrowbox.entity.ReportStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveReportDto {

    @NotNull(message = "Report resolution status is required")
    private ReportStatus status; // RESOLVED, DISMISSED, INVESTIGATING

    @Size(max = 2000, message = "Admin notes cannot exceed 2000 characters")
    private String adminNotes;

    // Optional moderation actions
    private boolean deactivateItem;
    private boolean banUser;
}
