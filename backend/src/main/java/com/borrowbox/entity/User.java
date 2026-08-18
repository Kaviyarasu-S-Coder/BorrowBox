package com.borrowbox.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_email", columnList = "email", unique = true),
        @Index(name = "idx_user_status", columnList = "is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String location;

    private Double latitude;

    private Double longitude;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // Reputation & Rating Aggregates
    @Builder.Default
    @Column(name = "average_rating")
    private Double averageRating = 5.0;

    @Builder.Default
    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Builder.Default
    @Column(name = "reputation_score")
    private Double reputationScore = 80.0; // Starts at 80 out of 100

    @Builder.Default
    @Column(name = "completed_borrowings")
    private Integer completedBorrowings = 0;

    @Builder.Default
    @Column(name = "completed_lendings")
    private Integer completedLendings = 0;

    @Builder.Default
    @Column(name = "cancellation_count")
    private Integer cancellationCount = 0;

    @Builder.Default
    @Column(name = "dispute_count")
    private Integer disputeCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
