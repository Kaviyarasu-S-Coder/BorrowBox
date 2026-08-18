package com.borrowbox.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "items", indexes = {
        @Index(name = "idx_item_owner", columnList = "owner_id"),
        @Index(name = "idx_item_category", columnList = "category_id"),
        @Index(name = "idx_item_status", columnList = "status"),
        @Index(name = "idx_item_location", columnList = "location"),
        @Index(name = "idx_item_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "subCategories"})
    private Category category;

    @Column(name = "sub_category", length = 100)
    private String subCategory;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ItemCondition condition;

    @Column(name = "estimated_value", precision = 10, scale = 2)
    private BigDecimal estimatedValue;

    @Column(name = "deposit_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal depositAmount = BigDecimal.ZERO;

    @Column(name = "daily_rate", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal dailyRate = BigDecimal.ZERO; // 0 for free community sharing

    @Column(name = "lending_mode", length = 30)
    @Builder.Default
    private String lendingMode = "FREE_BORROW"; // FREE_BORROW or DEPOSIT_ONLY or RENTAL

    @Column(nullable = false, length = 150)
    private String location; // Approximate locality e.g. "Indiranagar, Bangalore"

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ItemStatus status = ItemStatus.AVAILABLE;

    @Builder.Default
    @Column(name = "min_borrow_days")
    private Integer minBorrowDays = 1;

    @Builder.Default
    @Column(name = "max_borrow_days")
    private Integer maxBorrowDays = 14;

    @Column(name = "borrowing_rules", columnDefinition = "TEXT")
    private String borrowingRules;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemImage> images = new ArrayList<>();

    @Builder.Default
    @Column(name = "borrow_count")
    private Integer borrowCount = 0;

    @Builder.Default
    @Column(name = "view_count")
    private Integer viewCount = 0;

    // Optimistic Concurrency Control
    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addImage(ItemImage image) {
        images.add(image);
        image.setItem(this);
    }
}
