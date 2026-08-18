package com.borrowbox.service;

import com.borrowbox.dto.response.AdminStatsResponse;
import com.borrowbox.dto.response.AdminUserResponse;
import com.borrowbox.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    AdminStatsResponse getPlatformStats();

    Page<AdminUserResponse> getAllUsers(UserPrincipal adminUser, String search, Boolean isActive, Pageable pageable);

    AdminUserResponse toggleUserActiveStatus(UserPrincipal adminUser, Long userId);

    AdminUserResponse toggleUserVerification(UserPrincipal adminUser, Long userId);
}
