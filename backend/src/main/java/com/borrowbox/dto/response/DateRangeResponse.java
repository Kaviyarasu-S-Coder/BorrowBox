package com.borrowbox.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateRangeResponse {

    private LocalDate startDate;
    private LocalDate endDate;
    private String reason; // "BOOKED", "PENDING_CONFIRMATION", "MAINTENANCE"
}
