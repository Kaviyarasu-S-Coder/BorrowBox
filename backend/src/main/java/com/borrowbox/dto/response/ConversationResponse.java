package com.borrowbox.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

    private Long id;

    // Counterpart details (the other participant)
    private Long otherUserId;
    private String otherUserName;
    private String otherUserProfileImage;
    private Double otherUserRating;

    // Associated context
    private Long borrowRequestId;
    private Long transactionId;
    private Long itemId;
    private String itemTitle;
    private String itemPrimaryImage;

    // Last message preview
    private String lastMessage;
    private LocalDateTime lastMessageAt;

    private long unreadCount;
    private LocalDateTime createdAt;
}
