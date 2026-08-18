package com.borrowbox.dto.response;

import com.borrowbox.entity.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRequestResponse {

    private Long id;

    // Item summary
    private Long itemId;
    private String itemTitle;
    private String itemPrimaryImageUrl;
    private BigDecimal itemDeposit;
    private BigDecimal itemDailyRate;

    // Borrower summary
    private Long borrowerId;
    private String borrowerName;
    private String borrowerProfileImage;
    private Double borrowerReputation;
    private Double borrowerRating;

    // Owner summary
    private Long ownerId;
    private String ownerName;
    private String ownerProfileImage;

    private LocalDate startDate;
    private LocalDate endDate;
    private long totalDays;
    private String purpose;
    private String message;
    private RequestStatus status;
    private String responseMessage;
    private String cancellationReason;

    private Long transactionId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
