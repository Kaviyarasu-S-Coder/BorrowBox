package com.borrowbox.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;

    // Email and phone are populated ONLY for private profile owner view
    private String email;
    private String phone;

    private String fullName;
    private String bio;
    private String location;
    private String profileImageUrl;
    private boolean isVerified;
    private boolean isActive;

    private Double averageRating;
    private Integer ratingCount;
    private Double reputationScore;
    private Integer completedBorrowings;
    private Integer completedLendings;
    private Integer cancellationCount;
    private Integer disputeCount;

    private Set<String> roles;
    private LocalDateTime joinedDate;
}
