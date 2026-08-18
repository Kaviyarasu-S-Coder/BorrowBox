package com.borrowbox.service;

import com.borrowbox.dto.request.CreateDisputeDto;
import com.borrowbox.dto.request.ResolveDisputeDto;
import com.borrowbox.dto.response.DisputeResponse;
import com.borrowbox.entity.DisputeStatus;
import com.borrowbox.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DisputeService {

    DisputeResponse createDispute(UserPrincipal currentUser, CreateDisputeDto dto);

    DisputeResponse getDisputeById(UserPrincipal currentUser, Long disputeId);

    Page<DisputeResponse> getMyDisputes(UserPrincipal currentUser, Pageable pageable);

    Page<DisputeResponse> getAllDisputes(UserPrincipal currentUser, DisputeStatus status, Pageable pageable);

    DisputeResponse resolveDispute(UserPrincipal adminUser, Long disputeId, ResolveDisputeDto dto);
}
