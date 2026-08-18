package com.borrowbox.dto.request;

import com.borrowbox.entity.ItemCondition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemFilterCriteria {

    private String query;

    private Long categoryId;

    private String categorySlug;

    private List<ItemCondition> conditions;

    private BigDecimal minDeposit;

    private BigDecimal maxDeposit;

    private BigDecimal maxDailyRate;

    private String location;

    private String lendingMode;

    private Double userLat;

    private Double userLng;

    private Double maxDistanceKm;
}
