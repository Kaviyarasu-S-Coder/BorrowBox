package com.borrowbox.repository;

import com.borrowbox.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Page<Rating> findByToUserIdOrderByCreatedAtDesc(Long toUserId, Pageable pageable);

    List<Rating> findByTransactionId(Long transactionId);

    Optional<Rating> findByTransactionIdAndFromUserId(Long transactionId, Long fromUserId);

    boolean existsByTransactionIdAndFromUserId(Long transactionId, Long fromUserId);

    @Query("SELECT AVG(r.overallRating) FROM Rating r WHERE r.toUser.id = :userId")
    Double calculateAverageRatingForUser(@Param("userId") Long userId);

    long countByToUserId(Long toUserId);
}
