package com.borrowbox.service.impl;

import com.borrowbox.dto.response.AvailabilityCheckResponse;
import com.borrowbox.dto.response.DateRangeResponse;
import com.borrowbox.entity.BorrowTransaction;
import com.borrowbox.entity.Item;
import com.borrowbox.entity.ItemStatus;
import com.borrowbox.entity.TransactionStatus;
import com.borrowbox.exception.BadRequestException;
import com.borrowbox.exception.ResourceNotFoundException;
import com.borrowbox.repository.BorrowTransactionRepository;
import com.borrowbox.repository.ItemRepository;
import com.borrowbox.service.AvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvailabilityServiceImpl implements AvailabilityService {

    private final ItemRepository itemRepository;
    private final BorrowTransactionRepository transactionRepository;

    private static final List<TransactionStatus> BLOCKING_STATUSES = Arrays.asList(
            TransactionStatus.UPCOMING,
            TransactionStatus.READY_FOR_PICKUP,
            TransactionStatus.BORROWED,
            TransactionStatus.RETURN_PENDING
    );

    @Override
    @Transactional(readOnly = true)
    public AvailabilityCheckResponse checkAvailability(Long itemId, LocalDate startDate, LocalDate endDate) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

        if (item.getStatus() != ItemStatus.AVAILABLE) {
            return AvailabilityCheckResponse.builder()
                    .itemId(itemId)
                    .startDate(startDate)
                    .endDate(endDate)
                    .isAvailable(false)
                    .message("Item is currently not available for borrowing (Status: " + item.getStatus() + ").")
                    .depositRequired(item.getDepositAmount())
                    .dailyRate(item.getDailyRate())
                    .build();
        }

        if (startDate == null || endDate == null) {
            throw new BadRequestException("Start date and end date must not be null.");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Start date cannot be in the past.");
        }

        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be earlier than start date.");
        }

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        if (item.getMinBorrowDays() != null && totalDays < item.getMinBorrowDays()) {
            return AvailabilityCheckResponse.builder()
                    .itemId(itemId)
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalDays(totalDays)
                    .isAvailable(false)
                    .message("Minimum borrow duration is " + item.getMinBorrowDays() + " day(s). Requested: " + totalDays + " day(s).")
                    .depositRequired(item.getDepositAmount())
                    .dailyRate(item.getDailyRate())
                    .build();
        }

        if (item.getMaxBorrowDays() != null && totalDays > item.getMaxBorrowDays()) {
            return AvailabilityCheckResponse.builder()
                    .itemId(itemId)
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalDays(totalDays)
                    .isAvailable(false)
                    .message("Maximum borrow duration is " + item.getMaxBorrowDays() + " day(s). Requested: " + totalDays + " day(s).")
                    .depositRequired(item.getDepositAmount())
                    .dailyRate(item.getDailyRate())
                    .build();
        }

        // Overlap Math check: existing.startDate <= requested.endDate AND existing.endDate >= requested.startDate
        List<BorrowTransaction> conflicts = transactionRepository.findOverlappingActiveTransactions(
                itemId, BLOCKING_STATUSES, startDate, endDate
        );

        boolean available = conflicts.isEmpty();
        String message = available ? "Item is available for the selected dates!" : "Item is already booked for all or part of the selected period.";

        BigDecimal dailyRate = item.getDailyRate() != null ? item.getDailyRate() : BigDecimal.ZERO;
        BigDecimal rentalCost = dailyRate.multiply(BigDecimal.valueOf(totalDays));

        return AvailabilityCheckResponse.builder()
                .itemId(itemId)
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(totalDays)
                .isAvailable(available)
                .message(message)
                .depositRequired(item.getDepositAmount() != null ? item.getDepositAmount() : BigDecimal.ZERO)
                .dailyRate(dailyRate)
                .estimatedRentalCost(rentalCost)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DateRangeResponse> getBookedDateRanges(Long itemId, Integer year, Integer month) {
        if (!itemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Item", "id", itemId);
        }

        List<BorrowTransaction> activeTransactions = transactionRepository.findByItemIdAndStatusIn(itemId, BLOCKING_STATUSES);

        return activeTransactions.stream()
                .map(tx -> DateRangeResponse.builder()
                        .startDate(tx.getStartDate())
                        .endDate(tx.getEndDate())
                        .reason("BOOKED")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRangeAvailable(Long itemId, LocalDate startDate, LocalDate endDate) {
        List<BorrowTransaction> conflicts = transactionRepository.findOverlappingActiveTransactions(
                itemId, BLOCKING_STATUSES, startDate, endDate
        );
        return conflicts.isEmpty();
    }
}
