package com.borrowbox.dto.request;

import com.borrowbox.entity.ItemCondition;
import com.borrowbox.entity.ItemStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemRequest {

    @NotBlank(message = "Item title is required")
    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
    private String title;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private String subCategory;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 3000, message = "Description must be between 10 and 3000 characters")
    private String description;

    @NotNull(message = "Item condition is required")
    private ItemCondition condition;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal estimatedValue;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal depositAmount;

    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal dailyRate;

    private String lendingMode;

    @NotBlank(message = "Location is required")
    private String location;

    private Double latitude;
    private Double longitude;

    private Integer minBorrowDays;
    private Integer maxBorrowDays;

    private String borrowingRules;

    private ItemStatus status;
}
