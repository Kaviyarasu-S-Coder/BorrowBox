package com.borrowbox.service.impl;

import com.borrowbox.dto.request.CreateReportDto;
import com.borrowbox.dto.request.ResolveReportDto;
import com.borrowbox.dto.response.ReportResponse;
import com.borrowbox.entity.*;
import com.borrowbox.exception.BadRequestException;
import com.borrowbox.exception.ResourceNotFoundException;
import com.borrowbox.exception.UnauthorizedException;
import com.borrowbox.repository.ItemRepository;
import com.borrowbox.repository.ReportRepository;
import com.borrowbox.repository.UserRepository;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ReportResponse createReport(UserPrincipal currentUser, CreateReportDto dto) {
        if (currentUser == null) {
            throw new UnauthorizedException("Authentication required to file a report.");
        }

        User reportedBy = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        User reportedUser = null;
        if (dto.getReportedUserId() != null) {
            reportedUser = userRepository.findById(dto.getReportedUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getReportedUserId()));
        }

        Item reportedItem = null;
        if (dto.getReportedItemId() != null) {
            reportedItem = itemRepository.findById(dto.getReportedItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Item", "id", dto.getReportedItemId()));
        }

        if (reportedUser == null && reportedItem == null) {
            throw new BadRequestException("You must specify either a reported user or a reported item.");
        }

        Report report = Report.builder()
                .reportedBy(reportedBy)
                .reportedUser(reportedUser)
                .reportedItem(reportedItem)
                .reason(dto.getReason())
                .description(dto.getDescription())
                .status(ReportStatus.OPEN)
                .build();

        Report saved = reportRepository.save(report);
        log.info("Report ID={} submitted by user ID={} with reason '{}'", saved.getId(), reportedBy.getId(), dto.getReason());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportResponse> getAllReports(UserPrincipal currentUser, ReportStatus status, Pageable pageable) {
        if (status != null) {
            return reportRepository.findByStatus(status, pageable).map(this::mapToResponse);
        }
        return reportRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public ReportResponse resolveReport(UserPrincipal adminUser, Long reportId, ResolveReportDto dto) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));

        report.setStatus(dto.getStatus());
        report.setAdminNotes(dto.getAdminNotes());
        report.setResolvedAt(LocalDateTime.now());

        if (dto.isDeactivateItem() && report.getReportedItem() != null) {
            Item item = report.getReportedItem();
            item.setStatus(ItemStatus.INACTIVE);
            itemRepository.save(item);
            log.info("Admin ID={} deactivated reported item ID={}", adminUser.getId(), item.getId());
        }

        if (dto.isBanUser() && report.getReportedUser() != null) {
            User user = report.getReportedUser();
            user.setActive(false);
            userRepository.save(user);
            log.info("Admin ID={} suspended/banned reported user ID={}", adminUser.getId(), user.getId());
        }

        Report saved = reportRepository.save(report);
        log.info("Report ID={} resolved with status {}", saved.getId(), saved.getStatus());

        return mapToResponse(saved);
    }

    private ReportResponse mapToResponse(Report r) {
        return ReportResponse.builder()
                .id(r.getId())
                .reportedById(r.getReportedBy().getId())
                .reportedByName(r.getReportedBy().getFullName())
                .reportedUserId(r.getReportedUser() != null ? r.getReportedUser().getId() : null)
                .reportedUserName(r.getReportedUser() != null ? r.getReportedUser().getFullName() : null)
                .reportedItemId(r.getReportedItem() != null ? r.getReportedItem().getId() : null)
                .reportedItemTitle(r.getReportedItem() != null ? r.getReportedItem().getTitle() : null)
                .reason(r.getReason())
                .description(r.getDescription())
                .status(r.getStatus())
                .adminNotes(r.getAdminNotes())
                .resolvedAt(r.getResolvedAt())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
