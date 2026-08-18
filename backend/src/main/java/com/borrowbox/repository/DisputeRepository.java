package com.borrowbox.repository;

import com.borrowbox.entity.Dispute;
import com.borrowbox.entity.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    Page<Dispute> findByCreatedById(Long createdById, Pageable pageable);

    Page<Dispute> findByAgainstUserId(Long againstUserId, Pageable pageable);

    Page<Dispute> findByStatus(DisputeStatus status, Pageable pageable);

    Optional<Dispute> findByTransactionId(Long transactionId);

    long countByStatus(DisputeStatus status);
}
