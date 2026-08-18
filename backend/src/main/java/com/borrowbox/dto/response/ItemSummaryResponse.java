package com.borrowbox.dto.response;

import com.borrowbox.entity.ItemCondition;
import com.borrowbox.entity.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemSummaryResponse {

    private Long id;
    private String title;
    private String categoryName;
    private String categorySlug;
    private String subCategory;
    private ItemCondition condition;
    private BigDecimal estimatedValue;
    private BigDecimal depositAmount;
    private BigDecimal dailyRate;
    private String lendingMode;
    private String location;
    private ItemStatus status;
    private String primaryImageUrl;

    // Owner summary
    private Long ownerId;
    private String ownerName;
    private Double ownerRating;
    private Double ownerReputation;

    private Integer borrowCount;
    private LocalDateTime createdAt;
}
