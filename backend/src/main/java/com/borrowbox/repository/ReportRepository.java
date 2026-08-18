package com.borrowbox.repository;

import com.borrowbox.entity.Report;
import com.borrowbox.entity.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    Page<Report> findByReportedUserId(Long reportedUserId, Pageable pageable);

    Page<Report> findByReportedItemId(Long reportedItemId, Pageable pageable);

    long countByStatus(ReportStatus status);
}
