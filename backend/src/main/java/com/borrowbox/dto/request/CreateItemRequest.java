package com.borrowbox.dto.request;

import com.borrowbox.entity.ItemCondition;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemRequest {

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

    @DecimalMin(value = "0.0", inclusive = true, message = "Estimated value must be non-negative")
    private BigDecimal estimatedValue;

    @DecimalMin(value = "0.0", inclusive = true, message = "Deposit amount must be non-negative")
    @Builder.Default
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", inclusive = true, message = "Daily rate must be non-negative")
    @Builder.Default
    private BigDecimal dailyRate = BigDecimal.ZERO;

    @Builder.Default
    private String lendingMode = "FREE_BORROW";

    @NotBlank(message = "Location locality is required")
    private String location;

    private Double latitude;
    private Double longitude;

    @Min(value = 1, message = "Minimum borrow duration must be at least 1 day")
    @Builder.Default
    private Integer minBorrowDays = 1;

    @Min(value = 1, message = "Maximum borrow duration must be at least 1 day")
    @Builder.Default
    private Integer maxBorrowDays = 14;

    private String borrowingRules;

    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();
}
