package com.borrowbox.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {

    private Long id;
    private String email;
    private String fullName;
    private String location;
    private String profileImageUrl;
    private boolean isVerified;
    private Double averageRating;
    private Integer ratingCount;
    private Double reputationScore;
    private Set<String> roles;
}
