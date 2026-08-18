package com.borrowbox.dto.request;

import com.borrowbox.entity.DisputeStatus;
import jakarta.validation.constraints.NotBlank;
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
public class ResolveDisputeDto {

    @NotNull(message = "Resolution status is required")
    private DisputeStatus status; // RESOLVED_FAVOR_BORROWER, RESOLVED_FAVOR_OWNER, RESOLVED_MUTUAL_AGREEMENT, DISMISSED

    @NotBlank(message = "Admin decision summary is required")
    @Size(max = 500, message = "Decision summary cannot exceed 500 characters")
    private String adminDecision;

    @Size(max = 2000, message = "Resolution notes cannot exceed 2000 characters")
    private String resolutionNotes;
}
