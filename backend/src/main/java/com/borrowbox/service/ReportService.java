package com.borrowbox.service;

import com.borrowbox.dto.request.CreateReportDto;
import com.borrowbox.dto.request.ResolveReportDto;
import com.borrowbox.dto.response.ReportResponse;
import com.borrowbox.entity.ReportStatus;
import com.borrowbox.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReportService {

    ReportResponse createReport(UserPrincipal currentUser, CreateReportDto dto);

    Page<ReportResponse> getAllReports(UserPrincipal currentUser, ReportStatus status, Pageable pageable);

    ReportResponse resolveReport(UserPrincipal adminUser, Long reportId, ResolveReportDto dto);
}
