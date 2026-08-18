package com.borrowbox.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ratings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_rating_tx_from_to", columnNames = {"transaction_id", "from_user_id", "to_user_id"})
}, indexes = {
        @Index(name = "idx_rating_to_user", columnList = "to_user_id"),
        @Index(name = "idx_rating_tx", columnList = "transaction_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private BorrowTransaction transaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User toUser;

    @Column(name = "overall_rating", nullable = false)
    private Integer overallRating; // 1 to 5

    @Column(name = "communication_rating")
    private Integer communicationRating; // 1 to 5

    @Column(name = "punctuality_rating")
    private Integer punctualityRating; // 1 to 5

    @Column(name = "condition_rating")
    private Integer conditionRating; // 1 to 5

    @Column(name = "reliability_rating")
    private Integer reliabilityRating; // 1 to 5

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(length = 20)
    private String role; // "BORROWER_TO_OWNER" or "OWNER_TO_BORROWER"

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
