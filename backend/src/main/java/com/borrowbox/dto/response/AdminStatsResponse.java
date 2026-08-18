package com.borrowbox.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {

    // Users
    private long totalUsers;
    private long activeUsers;
    private long verifiedUsers;

    // Items
    private long totalItems;
    private long availableItems;
    private long borrowedItems;
    private long inactiveItems;

    // Transactions
    private long totalTransactions;
    private long completedTransactions;
    private long activeTransactions;
    private long overdueTransactions;
    private long disputedTransactions;
    private BigDecimal totalDepositHeld;

    // Moderation
    private long openDisputes;
    private long openReports;

    // Category distribution
    private Map<String, Long> categoryItemCounts;
}
