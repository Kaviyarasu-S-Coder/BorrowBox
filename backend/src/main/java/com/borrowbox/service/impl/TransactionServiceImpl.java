package com.borrowbox.service.impl;

import com.borrowbox.dto.request.ConfirmCodeDto;
import com.borrowbox.dto.request.RecordConditionDto;
import com.borrowbox.dto.response.TransactionConditionResponse;
import com.borrowbox.dto.response.TransactionResponse;
import com.borrowbox.entity.*;
import com.borrowbox.exception.BadRequestException;
import com.borrowbox.exception.ForbiddenException;
import com.borrowbox.exception.ResourceNotFoundException;
import com.borrowbox.repository.BorrowTransactionRepository;
import com.borrowbox.repository.ItemRepository;
import com.borrowbox.repository.TransactionConditionRepository;
import com.borrowbox.repository.UserRepository;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final BorrowTransactionRepository transactionRepository;
    private final TransactionConditionRepository conditionRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UserPrincipal currentUser, Long transactionId) {
        BorrowTransaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("BorrowTransaction", "id", transactionId));

        validateAccess(currentUser, tx);

        return mapToResponse(tx, currentUser);
    }

    @Override
    @Transactional
    public TransactionResponse confirmPickup(UserPrincipal currentUser, Long transactionId, ConfirmCodeDto dto) {
        BorrowTransaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("BorrowTransaction", "id", transactionId));

        validateAccess(currentUser, tx);

        if (tx.getStatus() != TransactionStatus.UPCOMING && tx.getStatus() != TransactionStatus.READY_FOR_PICKUP) {
            throw new BadRequestException("Pickup cannot be confirmed for transaction with status: " + tx.getStatus());
        }

        if (!tx.getPickupCode().equals(dto.getVerificationCode().trim())) {
            throw new BadRequestException("Invalid 6-digit pickup verification code.");
        }

        tx.setStatus(TransactionStatus.BORROWED);
        tx.setPickupTime(LocalDateTime.now());
        tx.setOwnerPickupConfirmed(true);
        tx.setBorrowerPickupConfirmed(true);
        if (dto.getNotes() != null) tx.setNotes(dto.getNotes());

        // Increment item's total borrow count
        Item item = tx.getItem();
        item.setBorrowCount(item.getBorrowCount() + 1);
        itemRepository.save(item);

        BorrowTransaction updated = transactionRepository.save(tx);
        log.info("Pickup confirmed for Transaction ID={}", updated.getId());

        return mapToResponse(updated, currentUser);
    }

    @Override
    @Transactional
    public TransactionResponse confirmReturn(UserPrincipal currentUser, Long transactionId, ConfirmCodeDto dto) {
        BorrowTransaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("BorrowTransaction", "id", transactionId));

        validateAccess(currentUser, tx);

        if (tx.getStatus() != TransactionStatus.BORROWED && tx.getStatus() != TransactionStatus.RETURN_PENDING && tx.getStatus() != TransactionStatus.OVERDUE) {
            throw new BadRequestException("Return cannot be confirmed for transaction with status: " + tx.getStatus());
        }

        if (!tx.getReturnCode().equals(dto.getVerificationCode().trim())) {
            throw new BadRequestException("Invalid 6-digit return verification code.");
        }

        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setReturnTime(LocalDateTime.now());
        tx.setOwnerReturnConfirmed(true);
        tx.setBorrowerReturnConfirmed(true);
        if (dto.getNotes() != null) tx.setNotes(dto.getNotes());

        // Increment user transaction counters & reputation
        User borrower = tx.getBorrower();
        borrower.setCompletedBorrowings(borrower.getCompletedBorrowings() + 1);
        borrower.setReputationScore(Math.min(100.0, borrower.getReputationScore() + 1.0));
        userRepository.save(borrower);

        User owner = tx.getOwner();
        owner.setCompletedLendings(owner.getCompletedLendings() + 1);
        owner.setReputationScore(Math.min(100.0, owner.getReputationScore() + 1.0));
        userRepository.save(owner);

        BorrowTransaction updated = transactionRepository.save(tx);
        log.info("Return confirmed for Transaction ID={}. Status marked COMPLETED.", updated.getId());

        return mapToResponse(updated, currentUser);
    }

    @Override
    @Transactional
    public TransactionConditionResponse recordCondition(UserPrincipal currentUser, Long transactionId, RecordConditionDto dto) {
        BorrowTransaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("BorrowTransaction", "id", transactionId));

        validateAccess(currentUser, tx);

        User recorder = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        String photosCsv = dto.getPhotoUrls() != null ? String.join(",", dto.getPhotoUrls()) : "";

        TransactionCondition condition = TransactionCondition.builder()
                .transaction(tx)
                .recordedBy(recorder)
                .stage(dto.getStage())
                .condition(dto.getCondition())
                .notes(dto.getNotes())
                .photoUrls(photosCsv)
                .build();

        TransactionCondition saved = conditionRepository.save(condition);
        log.info("Recorded condition stage={} for Transaction ID={}", saved.getStage(), tx.getId());

        return mapConditionToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getMyTransactions(UserPrincipal currentUser, Pageable pageable) {
        return transactionRepository.findByBorrowerId(currentUser.getId(), pageable)
                .map(tx -> mapToResponse(tx, currentUser));
    }

    private void validateAccess(UserPrincipal currentUser, BorrowTransaction tx) {
        boolean isBorrower = tx.getBorrower().getId().equals(currentUser.getId());
        boolean isOwner = tx.getOwner().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isBorrower && !isOwner && !isAdmin) {
            throw new ForbiddenException("You do not have permission to view or manage this transaction.");
        }
    }

    private TransactionConditionResponse mapConditionToResponse(TransactionCondition c) {
        List<String> photosList = c.getPhotoUrls() != null && !c.getPhotoUrls().isBlank()
                ? Arrays.asList(c.getPhotoUrls().split(","))
                : Collections.emptyList();

        return TransactionConditionResponse.builder()
                .id(c.getId())
                .stage(c.getStage())
                .condition(c.getCondition())
                .notes(c.getNotes())
                .recordedById(c.getRecordedBy().getId())
                .recordedByName(c.getRecordedBy().getFullName())
                .photos(photosList)
                .createdAt(c.getRecordedAt())
                .build();
    }

    private TransactionResponse mapToResponse(BorrowTransaction tx, UserPrincipal currentUser) {
        Item item = tx.getItem();
        User owner = tx.getOwner();
        User borrower = tx.getBorrower();

        String primaryImg = item.getImages() != null && !item.getImages().isEmpty()
                ? item.getImages().get(0).getImageUrl()
                : null;

        long days = ChronoUnit.DAYS.between(tx.getStartDate(), tx.getEndDate()) + 1;

        List<TransactionConditionResponse> condResponses = tx.getConditions() != null
                ? tx.getConditions().stream()
                .map(this::mapConditionToResponse)
                .collect(Collectors.toList())
                : List.of();

        return TransactionResponse.builder()
                .id(tx.getId())
                .borrowRequestId(tx.getBorrowRequest().getId())
                .itemId(item.getId())
                .itemTitle(item.getTitle())
                .itemPrimaryImageUrl(primaryImg)
                .depositHeld(tx.getDepositHeld())
                .dailyRate(item.getDailyRate())
                .ownerId(owner.getId())
                .ownerName(owner.getFullName())
                .ownerEmail(owner.getEmail())
                .ownerPhone(owner.getPhone())
                .ownerProfileImage(owner.getProfileImageUrl())
                .borrowerId(borrower.getId())
                .borrowerName(borrower.getFullName())
                .borrowerEmail(borrower.getEmail())
                .borrowerPhone(borrower.getPhone())
                .borrowerProfileImage(borrower.getProfileImageUrl())
                .startDate(tx.getStartDate())
                .endDate(tx.getEndDate())
                .totalDays(days)
                .handoverLocation(tx.getHandoverLocation())
                .status(tx.getStatus())
                .pickupCode(tx.getPickupCode())
                .returnCode(tx.getReturnCode())
                .pickupTime(tx.getPickupTime())
                .returnTime(tx.getReturnTime())
                .ownerPickupConfirmed(tx.isOwnerPickupConfirmed())
                .borrowerPickupConfirmed(tx.isBorrowerPickupConfirmed())
                .ownerReturnConfirmed(tx.isOwnerReturnConfirmed())
                .borrowerReturnConfirmed(tx.isBorrowerReturnConfirmed())
                .notes(tx.getNotes())
                .conditions(condResponses)
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt())
                .build();
    }
}
