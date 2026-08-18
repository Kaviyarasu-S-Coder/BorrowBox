package com.borrowbox.service.impl;

import com.borrowbox.dto.response.AdminStatsResponse;
import com.borrowbox.dto.response.AdminUserResponse;
import com.borrowbox.entity.*;
import com.borrowbox.exception.BadRequestException;
import com.borrowbox.exception.ResourceNotFoundException;
import com.borrowbox.repository.*;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final BorrowTransactionRepository transactionRepository;
    private final DisputeRepository disputeRepository;
    private final ReportRepository reportRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getPlatformStats() {
        // Users
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByIsActive(true);
        long verifiedUsers = userRepository.countByIsVerified(true);

        // Items
        long totalItems = itemRepository.count();
        long availableItems = itemRepository.countByStatus(ItemStatus.AVAILABLE);
        long borrowedItems = itemRepository.countByStatus(ItemStatus.BORROWED);
        long inactiveItems = itemRepository.countByStatus(ItemStatus.INACTIVE);

        // Transactions
        long totalTx = transactionRepository.count();
        long completedTx = transactionRepository.countByStatus(TransactionStatus.COMPLETED);
        long activeTx = transactionRepository.countByStatus(TransactionStatus.BORROWED)
                + transactionRepository.countByStatus(TransactionStatus.READY_FOR_PICKUP)
                + transactionRepository.countByStatus(TransactionStatus.UPCOMING);
        long overdueTx = transactionRepository.countByStatus(TransactionStatus.OVERDUE);
        long disputedTx = transactionRepository.countByStatus(TransactionStatus.DISPUTED);
        BigDecimal totalDeposit = transactionRepository.sumActiveDepositHeld();

        // Moderation
        long openDisputes = disputeRepository.countByStatus(DisputeStatus.OPEN);
        long openReports = reportRepository.countByStatus(ReportStatus.OPEN);

        // Categories
        Map<String, Long> catCounts = new HashMap<>();
        categoryRepository.findAll().forEach(cat -> {
            catCounts.put(cat.getName(), itemRepository.countByCategoryId(cat.getId()));
        });

        return AdminStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .verifiedUsers(verifiedUsers)
                .totalItems(totalItems)
                .availableItems(availableItems)
                .borrowedItems(borrowedItems)
                .inactiveItems(inactiveItems)
                .totalTransactions(totalTx)
                .completedTransactions(completedTx)
                .activeTransactions(activeTx)
                .overdueTransactions(overdueTx)
                .disputedTransactions(disputedTx)
                .totalDepositHeld(totalDeposit != null ? totalDeposit : BigDecimal.ZERO)
                .openDisputes(openDisputes)
                .openReports(openReports)
                .categoryItemCounts(catCounts)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getAllUsers(UserPrincipal adminUser, String search, Boolean isActive, Pageable pageable) {
        Page<User> users;
        if (isActive != null) {
            users = userRepository.findByIsActive(isActive, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }

        return users.map(this::mapToAdminUserResponse);
    }

    @Override
    @Transactional
    public AdminUserResponse toggleUserActiveStatus(UserPrincipal adminUser, Long userId) {
        if (adminUser.getId().equals(userId)) {
            throw new BadRequestException("You cannot toggle your own active/suspended status.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setActive(!user.isActive());
        User saved = userRepository.save(user);
        log.info("Admin ID={} toggled active status for User ID={} to {}", adminUser.getId(), userId, saved.isActive());

        return mapToAdminUserResponse(saved);
    }

    @Override
    @Transactional
    public AdminUserResponse toggleUserVerification(UserPrincipal adminUser, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setVerified(!user.isVerified());
        User saved = userRepository.save(user);
        log.info("Admin ID={} toggled verification status for User ID={} to {}", adminUser.getId(), userId, saved.isVerified());

        return mapToAdminUserResponse(saved);
    }

    private AdminUserResponse mapToAdminUserResponse(User u) {
        Set<String> roles = u.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return AdminUserResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .phone(u.getPhone())
                .location(u.getLocation())
                .profileImageUrl(u.getProfileImageUrl())
                .roles(roles)
                .verified(u.isVerified())
                .active(u.isActive())
                .averageRating(u.getAverageRating())
                .ratingCount(u.getRatingCount())
                .reputationScore(u.getReputationScore())
                .completedBorrowings(u.getCompletedBorrowings())
                .completedLendings(u.getCompletedLendings())
                .cancellationCount(u.getCancellationCount())
                .disputeCount(u.getDisputeCount())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
