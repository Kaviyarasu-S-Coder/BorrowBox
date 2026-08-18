package com.borrowbox.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_transactions", indexes = {
        @Index(name = "idx_tx_item", columnList = "item_id"),
        @Index(name = "idx_tx_borrower", columnList = "borrower_id"),
        @Index(name = "idx_tx_owner", columnList = "owner_id"),
        @Index(name = "idx_tx_status", columnList = "status"),
        @Index(name = "idx_tx_dates", columnList = "start_date, end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "borrow_request_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private BorrowRequest borrowRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "borrower_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User borrower;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User owner;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.UPCOMING;

    // Handover Verification Tokens (QR / 6-digit numeric OTP)
    @Column(name = "pickup_code", length = 10)
    private String pickupCode;

    @Column(name = "return_code", length = 10)
    private String returnCode;

    @Builder.Default
    @Column(name = "borrower_pickup_confirmed")
    private boolean borrowerPickupConfirmed = false;

    @Builder.Default
    @Column(name = "owner_pickup_confirmed")
    private boolean ownerPickupConfirmed = false;

    @Column(name = "pickup_time")
    private LocalDateTime pickupTime;

    @Builder.Default
    @Column(name = "borrower_return_confirmed")
    private boolean borrowerReturnConfirmed = false;

    @Builder.Default
    @Column(name = "owner_return_confirmed")
    private boolean ownerReturnConfirmed = false;

    @Column(name = "return_time")
    private LocalDateTime returnTime;

    @Column(name = "deposit_held", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal depositHeld = BigDecimal.ZERO;

    @Column(name = "handover_location", length = 200)
    private String handoverLocation;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<TransactionCondition> conditions = new java.util.ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
