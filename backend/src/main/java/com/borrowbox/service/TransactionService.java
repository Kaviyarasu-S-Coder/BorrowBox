package com.borrowbox.service;

import com.borrowbox.dto.request.ConfirmCodeDto;
import com.borrowbox.dto.request.RecordConditionDto;
import com.borrowbox.dto.response.TransactionConditionResponse;
import com.borrowbox.dto.response.TransactionResponse;
import com.borrowbox.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {

    TransactionResponse getTransactionById(UserPrincipal currentUser, Long transactionId);

    TransactionResponse confirmPickup(UserPrincipal currentUser, Long transactionId, ConfirmCodeDto dto);

    TransactionResponse confirmReturn(UserPrincipal currentUser, Long transactionId, ConfirmCodeDto dto);

    TransactionConditionResponse recordCondition(UserPrincipal currentUser, Long transactionId, RecordConditionDto dto);

    Page<TransactionResponse> getMyTransactions(UserPrincipal currentUser, Pageable pageable);
}
