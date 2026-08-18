package com.borrowbox.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityCheckResponse {

    private Long itemId;
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalDays;
    private boolean isAvailable;
    private String message;
    private BigDecimal depositRequired;
    private BigDecimal dailyRate;
    private BigDecimal estimatedRentalCost;
}
