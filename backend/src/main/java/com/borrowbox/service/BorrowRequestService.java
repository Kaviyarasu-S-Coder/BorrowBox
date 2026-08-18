package com.borrowbox.service;

import com.borrowbox.dto.request.CancelBorrowRequestDto;
import com.borrowbox.dto.request.CreateBorrowRequestDto;
import com.borrowbox.dto.request.RespondBorrowRequestDto;
import com.borrowbox.dto.response.BorrowRequestResponse;
import com.borrowbox.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BorrowRequestService {

    BorrowRequestResponse createBorrowRequest(UserPrincipal currentUser, CreateBorrowRequestDto dto);

    BorrowRequestResponse getBorrowRequestById(UserPrincipal currentUser, Long requestId);

    BorrowRequestResponse respondToBorrowRequest(UserPrincipal currentUser, Long requestId, RespondBorrowRequestDto dto);

    BorrowRequestResponse cancelBorrowRequest(UserPrincipal currentUser, Long requestId, CancelBorrowRequestDto dto);

    Page<BorrowRequestResponse> getMySentRequests(UserPrincipal currentUser, Pageable pageable);

    Page<BorrowRequestResponse> getMyReceivedRequests(UserPrincipal currentUser, Pageable pageable);
}
