package com.borrowbox.dto.response;

import com.borrowbox.entity.ItemCondition;
import com.borrowbox.entity.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {

    private Long id;
    private String title;
    private String description;
    private Long categoryId;
    private String categoryName;
    private String categorySlug;
    private String subCategory;
    private ItemCondition condition;
    private BigDecimal estimatedValue;
    private BigDecimal depositAmount;
    private BigDecimal dailyRate;
    private String lendingMode;
    private String location;
    private Double latitude;
    private Double longitude;
    private ItemStatus status;

    private Integer minBorrowDays;
    private Integer maxBorrowDays;
    private String borrowingRules;

    @Builder.Default
    private List<ItemImageResponse> images = new ArrayList<>();

    // Owner info
    private Long ownerId;
    private String ownerName;
    private String ownerProfileImage;
    private String ownerLocation;
    private Double ownerRating;
    private Integer ownerRatingCount;
    private Double ownerReputation;
    private Integer ownerCompletedLendings;
    private LocalDateTime ownerJoinedDate;

    private Integer borrowCount;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
