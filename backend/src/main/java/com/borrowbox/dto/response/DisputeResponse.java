package com.borrowbox.dto.response;

import com.borrowbox.entity.DisputeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisputeResponse {

    private Long id;
    private Long transactionId;

    private Long itemId;
    private String itemTitle;

    private Long createdById;
    private String createdByName;

    private Long againstUserId;
    private String againstUserName;

    private String reason;
    private String description;
    private List<String> evidenceImages;

    private DisputeStatus status;
    private String adminDecision;
    private String resolutionNotes;

    private Long resolvedById;
    private String resolvedByName;
    private LocalDateTime resolvedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
