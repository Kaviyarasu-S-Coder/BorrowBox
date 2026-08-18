package com.borrowbox.dto.response;

import com.borrowbox.entity.NotificationType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private String linkUrl;
    private Long referenceId;

    @JsonProperty("isRead")
    private boolean isRead;

    private LocalDateTime createdAt;
}
