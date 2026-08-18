package com.borrowbox.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRatingDto {

    @NotNull(message = "Transaction ID is required")
    private Long transactionId;

    @NotNull(message = "Overall rating is required")
    @Min(value = 1, message = "Rating must be at least 1 star")
    @Max(value = 5, message = "Rating cannot exceed 5 stars")
    private Integer rating;

    @Min(value = 1, message = "Communication rating must be at least 1 star")
    @Max(value = 5, message = "Communication rating cannot exceed 5 stars")
    private Integer communicationRating;

    @Min(value = 1, message = "Punctuality rating must be at least 1 star")
    @Max(value = 5, message = "Punctuality rating cannot exceed 5 stars")
    private Integer punctualityRating;

    @Min(value = 1, message = "Condition/Reliability rating must be at least 1 star")
    @Max(value = 5, message = "Condition/Reliability rating cannot exceed 5 stars")
    private Integer conditionRating;

    @Size(max = 1000, message = "Review comment cannot exceed 1000 characters")
    private String review;
}
