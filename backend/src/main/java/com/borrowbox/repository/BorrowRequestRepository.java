package com.borrowbox.repository;

import com.borrowbox.entity.BorrowRequest;
import com.borrowbox.entity.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {

    Page<BorrowRequest> findByBorrowerId(Long borrowerId, Pageable pageable);

    Page<BorrowRequest> findByOwnerId(Long ownerId, Pageable pageable);

    Page<BorrowRequest> findByOwnerIdAndStatus(Long ownerId, RequestStatus status, Pageable pageable);

    List<BorrowRequest> findByItemIdAndStatus(Long itemId, RequestStatus status);

    long countByBorrowerIdAndStatus(Long borrowerId, RequestStatus status);

    long countByOwnerIdAndStatus(Long ownerId, RequestStatus status);

    // Overlap Query for pending/accepted requests: (req.startDate <= :endDate) AND (req.endDate >= :startDate)
    @Query("SELECT r FROM BorrowRequest r WHERE r.item.id = :itemId AND r.status = :status AND " +
            "r.startDate <= :endDate AND r.endDate >= :startDate")
    List<BorrowRequest> findConflictingRequests(
            @Param("itemId") Long itemId,
            @Param("status") RequestStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Find other pending requests for the same item during a specific window to auto-reject upon acceptance
    @Query("SELECT r FROM BorrowRequest r WHERE r.item.id = :itemId AND r.id <> :excludeRequestId AND " +
            "r.status = 'PENDING' AND r.startDate <= :endDate AND r.endDate >= :startDate")
    List<BorrowRequest> findOverlappingPendingRequests(
            @Param("itemId") Long itemId,
            @Param("excludeRequestId") Long excludeRequestId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
