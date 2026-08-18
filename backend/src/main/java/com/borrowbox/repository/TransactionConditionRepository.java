package com.borrowbox.repository;

import com.borrowbox.entity.ConditionStage;
import com.borrowbox.entity.TransactionCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionConditionRepository extends JpaRepository<TransactionCondition, Long> {

    List<TransactionCondition> findByTransactionIdOrderByRecordedAtAsc(Long transactionId);

    Optional<TransactionCondition> findByTransactionIdAndStage(Long transactionId, ConditionStage stage);
}
