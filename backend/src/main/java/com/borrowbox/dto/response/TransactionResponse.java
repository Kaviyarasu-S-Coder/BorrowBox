package com.borrowbox.dto.response;

import com.borrowbox.entity.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private Long borrowRequestId;

    // Item details
    private Long itemId;
    private String itemTitle;
    private String itemPrimaryImageUrl;
    private BigDecimal depositHeld;
    private BigDecimal dailyRate;

    // Parties
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;
    private String ownerProfileImage;

    private Long borrowerId;
    private String borrowerName;
    private String borrowerEmail;
    private String borrowerPhone;
    private String borrowerProfileImage;

    private LocalDate startDate;
    private LocalDate endDate;
    private long totalDays;

    private String handoverLocation;
    private TransactionStatus status;

    // Pickup & Return verification
    private String pickupCode; // Only shown to authorized viewer
    private String returnCode; // Only shown to authorized viewer
    private LocalDateTime pickupTime;
    private LocalDateTime returnTime;
    private boolean ownerPickupConfirmed;
    private boolean borrowerPickupConfirmed;
    private boolean ownerReturnConfirmed;
    private boolean borrowerReturnConfirmed;

    private String notes;

    @Builder.Default
    private List<TransactionConditionResponse> conditions = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
