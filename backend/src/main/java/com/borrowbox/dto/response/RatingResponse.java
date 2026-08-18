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
public class RatingResponse {

    private Long id;
    private Long transactionId;

    private Long fromUserId;
    private String fromUserName;
    private String fromUserProfileImage;

    private Long toUserId;
    private String toUserName;

    private Integer rating;
    private Integer communicationRating;
    private Integer punctualityRating;
    private Integer conditionRating;
    private Integer reliabilityRating;
    private String review;

    private LocalDateTime createdAt;
}
