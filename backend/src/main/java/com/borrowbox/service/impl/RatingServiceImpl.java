package com.borrowbox.service.impl;

import com.borrowbox.dto.request.CreateRatingDto;
import com.borrowbox.dto.response.RatingResponse;
import com.borrowbox.entity.*;
import com.borrowbox.exception.BadRequestException;
import com.borrowbox.exception.ConflictException;
import com.borrowbox.exception.ForbiddenException;
import com.borrowbox.exception.ResourceNotFoundException;
import com.borrowbox.repository.BorrowTransactionRepository;
import com.borrowbox.repository.RatingRepository;
import com.borrowbox.repository.UserRepository;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.NotificationService;
import com.borrowbox.service.RatingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final BorrowTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public RatingResponse createRating(UserPrincipal currentUser, CreateRatingDto dto) {
        BorrowTransaction tx = transactionRepository.findById(dto.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("BorrowTransaction", "id", dto.getTransactionId()));

        if (tx.getStatus() != TransactionStatus.COMPLETED) {
            throw new BadRequestException("Ratings can only be submitted after transaction is COMPLETED.");
        }

        boolean isBorrower = tx.getBorrower().getId().equals(currentUser.getId());
        boolean isOwner = tx.getOwner().getId().equals(currentUser.getId());

        if (!isBorrower && !isOwner) {
            throw new ForbiddenException("You were not a participant in this transaction.");
        }

        if (ratingRepository.existsByTransactionIdAndFromUserId(tx.getId(), currentUser.getId())) {
            throw new ConflictException("You have already submitted a rating for this transaction.");
        }

        User fromUser = isBorrower ? tx.getBorrower() : tx.getOwner();
        User toUser = isBorrower ? tx.getOwner() : tx.getBorrower();
        String role = isBorrower ? "BORROWER_TO_OWNER" : "OWNER_TO_BORROWER";

        Rating rating = Rating.builder()
                .transaction(tx)
                .fromUser(fromUser)
                .toUser(toUser)
                .overallRating(dto.getRating())
                .communicationRating(dto.getCommunicationRating())
                .punctualityRating(dto.getPunctualityRating())
                .conditionRating(dto.getConditionRating())
                .reliabilityRating(dto.getConditionRating())
                .comment(dto.getReview())
                .role(role)
                .build();

        Rating saved = ratingRepository.save(rating);

        // Recompute User Stats & Weighted Reputation
        updateUserReputationAndRating(toUser);

        // Send notification
        notificationService.createNotification(
                toUser,
                NotificationType.RATING_RECEIVED,
                "New Rating Received",
                fromUser.getFullName() + " gave you a " + dto.getRating() + "-star rating!",
                "/users/" + toUser.getId(),
                saved.getId()
        );

        log.info("Created rating ID={} from user ID={} to user ID={} for tx ID={}", saved.getId(), fromUser.getId(), toUser.getId(), tx.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RatingResponse> getUserRatings(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        return ratingRepository.findByToUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    private void updateUserReputationAndRating(User user) {
        Double avgRating = ratingRepository.calculateAverageRatingForUser(user.getId());
        long count = ratingRepository.countByToUserId(user.getId());

        double avg = (avgRating != null) ? Math.round(avgRating * 10.0) / 10.0 : 5.0;
        user.setAverageRating(avg);
        user.setRatingCount((int) count);

        // Weighted Reputation Formula
        int totalCompleted = (user.getCompletedBorrowings() != null ? user.getCompletedBorrowings() : 0)
                + (user.getCompletedLendings() != null ? user.getCompletedLendings() : 0);
        int cancellations = user.getCancellationCount() != null ? user.getCancellationCount() : 0;
        int disputes = user.getDisputeCount() != null ? user.getDisputeCount() : 0;

        double reputation = 50.0 + ((avg - 3.0) * 10.0) + (totalCompleted * 2.0) - (cancellations * 5.0) - (disputes * 10.0);
        reputation = Math.min(100.0, Math.max(0.0, reputation));
        user.setReputationScore(Math.round(reputation * 10.0) / 10.0);

        userRepository.save(user);
    }

    private RatingResponse mapToResponse(Rating r) {
        return RatingResponse.builder()
                .id(r.getId())
                .transactionId(r.getTransaction().getId())
                .fromUserId(r.getFromUser().getId())
                .fromUserName(r.getFromUser().getFullName())
                .fromUserProfileImage(r.getFromUser().getProfileImageUrl())
                .toUserId(r.getToUser().getId())
                .toUserName(r.getToUser().getFullName())
                .rating(r.getOverallRating())
                .communicationRating(r.getCommunicationRating())
                .punctualityRating(r.getPunctualityRating())
                .conditionRating(r.getConditionRating())
                .reliabilityRating(r.getReliabilityRating())
                .review(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
