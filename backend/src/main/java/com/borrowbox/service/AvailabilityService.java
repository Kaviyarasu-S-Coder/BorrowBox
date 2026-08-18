package com.borrowbox.service;

import com.borrowbox.dto.response.AvailabilityCheckResponse;
import com.borrowbox.dto.response.DateRangeResponse;

import java.time.LocalDate;
import java.util.List;

public interface AvailabilityService {

    AvailabilityCheckResponse checkAvailability(Long itemId, LocalDate startDate, LocalDate endDate);

    List<DateRangeResponse> getBookedDateRanges(Long itemId, Integer year, Integer month);

    boolean isRangeAvailable(Long itemId, LocalDate startDate, LocalDate endDate);
}
