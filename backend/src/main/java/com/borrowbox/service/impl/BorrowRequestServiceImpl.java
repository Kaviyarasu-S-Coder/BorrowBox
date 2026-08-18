package com.borrowbox.service.impl;

import com.borrowbox.dto.request.CancelBorrowRequestDto;
import com.borrowbox.dto.request.CreateBorrowRequestDto;
import com.borrowbox.dto.request.RespondBorrowRequestDto;
import com.borrowbox.dto.response.AvailabilityCheckResponse;
import com.borrowbox.dto.response.BorrowRequestResponse;
import com.borrowbox.entity.*;
import com.borrowbox.exception.BadRequestException;
import com.borrowbox.exception.ConflictException;
import com.borrowbox.exception.ForbiddenException;
import com.borrowbox.exception.ResourceNotFoundException;
import com.borrowbox.repository.*;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.AvailabilityService;
import com.borrowbox.service.BorrowRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowRequestServiceImpl implements BorrowRequestService {

    private final BorrowRequestRepository borrowRequestRepository;
    private final BorrowTransactionRepository transactionRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final AvailabilityService availabilityService;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    @Transactional
    public BorrowRequestResponse createBorrowRequest(UserPrincipal currentUser, CreateBorrowRequestDto dto) {
        User borrower = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", dto.getItemId()));

        if (item.getOwner().getId().equals(borrower.getId())) {
            throw new BadRequestException("You cannot borrow your own listed item.");
        }

        if (item.getStatus() != ItemStatus.AVAILABLE) {
            throw new BadRequestException("This item is currently unavailable for borrowing.");
        }

        // Validate availability and date rules
        AvailabilityCheckResponse check = availabilityService.checkAvailability(item.getId(), dto.getStartDate(), dto.getEndDate());
        if (!check.isAvailable()) {
            throw new ConflictException(check.getMessage());
        }

        BorrowRequest request = BorrowRequest.builder()
                .item(item)
                .borrower(borrower)
                .owner(item.getOwner())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .purpose(dto.getPurpose().trim())
                .message(dto.getMessage().trim())
                .status(RequestStatus.PENDING)
                .build();

        BorrowRequest saved = borrowRequestRepository.save(request);
        log.info("Created BorrowRequest ID={} from borrower ID={} to owner ID={}", saved.getId(), borrower.getId(), item.getOwner().getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BorrowRequestResponse getBorrowRequestById(UserPrincipal currentUser, Long requestId) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BorrowRequest", "id", requestId));

        validateAccess(currentUser, request);

        return mapToResponse(request);
    }

    @Override
    @Transactional
    public BorrowRequestResponse respondToBorrowRequest(UserPrincipal currentUser, Long requestId, RespondBorrowRequestDto dto) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BorrowRequest", "id", requestId));

        if (!request.getOwner().getId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new ForbiddenException("Only the item owner can accept or reject this borrow request.");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Cannot respond to request with status: " + request.getStatus());
        }

        if (Boolean.TRUE.equals(dto.getAccept())) {
            // Re-validate availability
            boolean isAvailable = availabilityService.isRangeAvailable(request.getItem().getId(), request.getStartDate(), request.getEndDate());
            if (!isAvailable) {
                throw new ConflictException("Item has already been booked for this overlapping date interval.");
            }

            request.setStatus(RequestStatus.ACCEPTED);
            request.setResponseMessage(dto.getResponseMessage());

            // Initialize BorrowTransaction
            String pickupCode = generateVerificationCode();
            String returnCode = generateVerificationCode();

            BorrowTransaction transaction = BorrowTransaction.builder()
                    .borrowRequest(request)
                    .item(request.getItem())
                    .borrower(request.getBorrower())
                    .owner(request.getOwner())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .pickupCode(pickupCode)
                    .returnCode(returnCode)
                    .depositHeld(request.getItem().getDepositAmount())
                    .handoverLocation(request.getItem().getLocation())
                    .status(TransactionStatus.UPCOMING)
                    .build();

            BorrowTransaction savedTx = transactionRepository.save(transaction);
            request.setTransaction(savedTx);

            log.info("Accepted BorrowRequest ID={} and initialized BorrowTransaction ID={}", request.getId(), savedTx.getId());
        } else {
            request.setStatus(RequestStatus.REJECTED);
            request.setResponseMessage(dto.getResponseMessage());
            log.info("Rejected BorrowRequest ID={}", request.getId());
        }

        BorrowRequest saved = borrowRequestRepository.save(request);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BorrowRequestResponse cancelBorrowRequest(UserPrincipal currentUser, Long requestId, CancelBorrowRequestDto dto) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("BorrowRequest", "id", requestId));

        if (!request.getBorrower().getId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new ForbiddenException("Only the borrower can cancel this request.");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BadRequestException("Only pending requests can be cancelled directly.");
        }

        request.setStatus(RequestStatus.CANCELLED);
        request.setCancellationReason(dto.getCancellationReason().trim());

        BorrowRequest saved = borrowRequestRepository.save(request);
        log.info("Cancelled BorrowRequest ID={}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowRequestResponse> getMySentRequests(UserPrincipal currentUser, Pageable pageable) {
        return borrowRequestRepository.findByBorrowerId(currentUser.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowRequestResponse> getMyReceivedRequests(UserPrincipal currentUser, Pageable pageable) {
        return borrowRequestRepository.findByOwnerId(currentUser.getId(), pageable)
                .map(this::mapToResponse);
    }

    private String generateVerificationCode() {
        int code = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    private void validateAccess(UserPrincipal currentUser, BorrowRequest request) {
        boolean isBorrower = request.getBorrower().getId().equals(currentUser.getId());
        boolean isOwner = request.getOwner().getId().equals(currentUser.getId());
        if (!isBorrower && !isOwner && !isAdmin(currentUser)) {
            throw new ForbiddenException("You do not have permission to view this borrow request.");
        }
    }

    private boolean isAdmin(UserPrincipal currentUser) {
        return currentUser != null && currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private BorrowRequestResponse mapToResponse(BorrowRequest req) {
        Item item = req.getItem();
        User borrower = req.getBorrower();
        User owner = req.getOwner();

        String primaryImg = item.getImages() != null && !item.getImages().isEmpty()
                ? item.getImages().get(0).getImageUrl()
                : null;

        long days = ChronoUnit.DAYS.between(req.getStartDate(), req.getEndDate()) + 1;

        return BorrowRequestResponse.builder()
                .id(req.getId())
                .itemId(item.getId())
                .itemTitle(item.getTitle())
                .itemPrimaryImageUrl(primaryImg)
                .itemDeposit(item.getDepositAmount())
                .itemDailyRate(item.getDailyRate())
                .borrowerId(borrower.getId())
                .borrowerName(borrower.getFullName())
                .borrowerProfileImage(borrower.getProfileImageUrl())
                .borrowerReputation(borrower.getReputationScore())
                .borrowerRating(borrower.getAverageRating())
                .ownerId(owner.getId())
                .ownerName(owner.getFullName())
                .ownerProfileImage(owner.getProfileImageUrl())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .totalDays(days)
                .purpose(req.getPurpose())
                .message(req.getMessage())
                .status(req.getStatus())
                .responseMessage(req.getResponseMessage())
                .cancellationReason(req.getCancellationReason())
                .transactionId(req.getTransaction() != null ? req.getTransaction().getId() : null)
                .createdAt(req.getCreatedAt())
                .updatedAt(req.getUpdatedAt())
                .build();
    }
}
