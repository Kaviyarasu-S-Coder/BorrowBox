package com.borrowbox.service.impl;

import com.borrowbox.dto.request.CreateDisputeDto;
import com.borrowbox.dto.request.ResolveDisputeDto;
import com.borrowbox.dto.response.DisputeResponse;
import com.borrowbox.entity.*;
import com.borrowbox.exception.BadRequestException;
import com.borrowbox.exception.ConflictException;
import com.borrowbox.exception.ForbiddenException;
import com.borrowbox.exception.ResourceNotFoundException;
import com.borrowbox.repository.BorrowTransactionRepository;
import com.borrowbox.repository.DisputeRepository;
import com.borrowbox.repository.UserRepository;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.DisputeService;
import com.borrowbox.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputeRepository;
    private final BorrowTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public DisputeResponse createDispute(UserPrincipal currentUser, CreateDisputeDto dto) {
        BorrowTransaction tx = transactionRepository.findById(dto.getTransactionId())
                .orElseThrow(() -> new ResourceNotFoundException("BorrowTransaction", "id", dto.getTransactionId()));

        boolean isBorrower = tx.getBorrower().getId().equals(currentUser.getId());
        boolean isOwner = tx.getOwner().getId().equals(currentUser.getId());

        if (!isBorrower && !isOwner) {
            throw new ForbiddenException("You are not a participant in this transaction.");
        }

        if (disputeRepository.findByTransactionId(tx.getId()).isPresent()) {
            throw new ConflictException("A dispute has already been filed for this transaction.");
        }

        User createdBy = isBorrower ? tx.getBorrower() : tx.getOwner();
        User againstUser = isBorrower ? tx.getOwner() : tx.getBorrower();

        String evidenceStr = (dto.getEvidenceImages() != null && !dto.getEvidenceImages().isEmpty())
                ? String.join(",", dto.getEvidenceImages())
                : null;

        Dispute dispute = Dispute.builder()
                .transaction(tx)
                .createdBy(createdBy)
                .againstUser(againstUser)
                .reason(dto.getReason())
                .description(dto.getDescription())
                .evidenceImages(evidenceStr)
                .status(DisputeStatus.OPEN)
                .build();

        Dispute saved = disputeRepository.save(dispute);

        // Update transaction status
        tx.setStatus(TransactionStatus.DISPUTED);
        transactionRepository.save(tx);

        // Notify counterpart
        notificationService.createNotification(
                againstUser,
                NotificationType.DISPUTE_OPENED,
                "Dispute Filed on Transaction #" + tx.getId(),
                createdBy.getFullName() + " filed a dispute: '" + dto.getReason() + "'. Our admin team is reviewing it.",
                "/transactions/" + tx.getId(),
                saved.getId()
        );

        log.info("Dispute ID={} raised by user ID={} against user ID={} for tx ID={}",
                saved.getId(), createdBy.getId(), againstUser.getId(), tx.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DisputeResponse getDisputeById(UserPrincipal currentUser, Long disputeId) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute", "id", disputeId));

        boolean isCreator = dispute.getCreatedBy().getId().equals(currentUser.getId());
        boolean isAgainst = dispute.getAgainstUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isCreator && !isAgainst && !isAdmin) {
            throw new ForbiddenException("You do not have permission to view this dispute.");
        }

        return mapToResponse(dispute);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DisputeResponse> getMyDisputes(UserPrincipal currentUser, Pageable pageable) {
        return disputeRepository.findByCreatedById(currentUser.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DisputeResponse> getAllDisputes(UserPrincipal currentUser, DisputeStatus status, Pageable pageable) {
        if (status != null) {
            return disputeRepository.findByStatus(status, pageable).map(this::mapToResponse);
        }
        return disputeRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public DisputeResponse resolveDispute(UserPrincipal adminUser, Long disputeId, ResolveDisputeDto dto) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute", "id", disputeId));

        User admin = userRepository.findById(adminUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminUser.getId()));

        dispute.setStatus(dto.getStatus());
        dispute.setAdminDecision(dto.getAdminDecision());
        dispute.setResolutionNotes(dto.getResolutionNotes());
        dispute.setResolvedBy(admin);
        dispute.setResolvedAt(LocalDateTime.now());

        // Penalty application for against user if resolved against them
        if (dto.getStatus() == DisputeStatus.RESOLVED) {
            User againstUser = dispute.getAgainstUser();
            againstUser.setDisputeCount((againstUser.getDisputeCount() != null ? againstUser.getDisputeCount() : 0) + 1);
            recalculateReputation(againstUser);
            userRepository.save(againstUser);
        }

        // Complete transaction
        BorrowTransaction tx = dispute.getTransaction();
        tx.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(tx);

        Dispute saved = disputeRepository.save(dispute);

        // Notify both parties
        notificationService.createNotification(
                dispute.getCreatedBy(),
                NotificationType.DISPUTE_UPDATED,
                "Dispute Resolved",
                "Your dispute has been resolved: " + dto.getAdminDecision(),
                "/transactions/" + tx.getId(),
                saved.getId()
        );

        notificationService.createNotification(
                dispute.getAgainstUser(),
                NotificationType.DISPUTE_UPDATED,
                "Dispute Resolved",
                "The dispute on transaction #" + tx.getId() + " has been resolved: " + dto.getAdminDecision(),
                "/transactions/" + tx.getId(),
                saved.getId()
        );

        log.info("Dispute ID={} resolved by admin ID={} with status {}", saved.getId(), admin.getId(), saved.getStatus());

        return mapToResponse(saved);
    }

    private void recalculateReputation(User user) {
        double avg = user.getAverageRating() != null ? user.getAverageRating() : 5.0;
        int totalCompleted = (user.getCompletedBorrowings() != null ? user.getCompletedBorrowings() : 0)
                + (user.getCompletedLendings() != null ? user.getCompletedLendings() : 0);
        int cancellations = user.getCancellationCount() != null ? user.getCancellationCount() : 0;
        int disputes = user.getDisputeCount() != null ? user.getDisputeCount() : 0;

        double reputation = 50.0 + ((avg - 3.0) * 10.0) + (totalCompleted * 2.0) - (cancellations * 5.0) - (disputes * 10.0);
        reputation = Math.min(100.0, Math.max(0.0, reputation));
        user.setReputationScore(Math.round(reputation * 10.0) / 10.0);
    }

    private DisputeResponse mapToResponse(Dispute d) {
        List<String> images = d.getEvidenceImages() != null && !d.getEvidenceImages().isBlank()
                ? Arrays.asList(d.getEvidenceImages().split(","))
                : Collections.emptyList();

        return DisputeResponse.builder()
                .id(d.getId())
                .transactionId(d.getTransaction().getId())
                .itemId(d.getTransaction().getItem().getId())
                .itemTitle(d.getTransaction().getItem().getTitle())
                .createdById(d.getCreatedBy().getId())
                .createdByName(d.getCreatedBy().getFullName())
                .againstUserId(d.getAgainstUser().getId())
                .againstUserName(d.getAgainstUser().getFullName())
                .reason(d.getReason())
                .description(d.getDescription())
                .evidenceImages(images)
                .status(d.getStatus())
                .adminDecision(d.getAdminDecision())
                .resolutionNotes(d.getResolutionNotes())
                .resolvedById(d.getResolvedBy() != null ? d.getResolvedBy().getId() : null)
                .resolvedByName(d.getResolvedBy() != null ? d.getResolvedBy().getFullName() : null)
                .resolvedAt(d.getResolvedAt())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}
