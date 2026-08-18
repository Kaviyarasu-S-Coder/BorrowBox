package com.borrowbox.repository;

import com.borrowbox.entity.BorrowTransaction;
import com.borrowbox.entity.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowTransactionRepository extends JpaRepository<BorrowTransaction, Long> {

    Optional<BorrowTransaction> findByBorrowRequestId(Long borrowRequestId);

    Page<BorrowTransaction> findByBorrowerId(Long borrowerId, Pageable pageable);

    Page<BorrowTransaction> findByOwnerId(Long ownerId, Pageable pageable);

    Page<BorrowTransaction> findByStatus(TransactionStatus status, Pageable pageable);

    List<BorrowTransaction> findByItemIdAndStatusIn(Long itemId, Collection<TransactionStatus> statuses);

    // Overlap Query for active bookings on an item
    @Query("SELECT t FROM BorrowTransaction t WHERE t.item.id = :itemId AND " +
            "t.status IN :statuses AND " +
            "t.startDate <= :endDate AND t.endDate >= :startDate")
    List<BorrowTransaction> findOverlappingActiveTransactions(
            @Param("itemId") Long itemId,
            @Param("statuses") Collection<TransactionStatus> statuses,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Scheduled Overdue Query: End date is in the past and transaction is still active (not RETURNED/COMPLETED/CANCELLED)
    @Query("SELECT t FROM BorrowTransaction t WHERE t.endDate < :today AND t.status = 'BORROWED'")
    List<BorrowTransaction> findTransactionsDueForOverdue(@Param("today") LocalDate today);

    // Return reminder query: End date is tomorrow
    @Query("SELECT t FROM BorrowTransaction t WHERE t.endDate = :reminderDate AND t.status = 'BORROWED'")
    List<BorrowTransaction> findTransactionsDueForReminder(@Param("reminderDate") LocalDate reminderDate);

    long countByStatus(TransactionStatus status);

    long countByBorrowerIdAndStatus(Long borrowerId, TransactionStatus status);

    long countByOwnerIdAndStatus(Long ownerId, TransactionStatus status);
}
