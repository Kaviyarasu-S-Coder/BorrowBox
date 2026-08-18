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
public class AdminUserResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String location;
    private String profileImageUrl;
    private Set<String> roles;
    private boolean verified;
    private boolean active;
    private Double averageRating;
    private Integer ratingCount;
    private Double reputationScore;
    private Integer completedBorrowings;
    private Integer completedLendings;
    private Integer cancellationCount;
    private Integer disputeCount;
    private LocalDateTime createdAt;
}
