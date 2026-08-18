package com.borrowbox.service;

import com.borrowbox.dto.response.NotificationResponse;
import com.borrowbox.dto.response.UnreadCountResponse;
import com.borrowbox.entity.NotificationType;
import com.borrowbox.entity.User;
import com.borrowbox.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    NotificationResponse createNotification(
            User recipient,
            NotificationType type,
            String title,
            String message,
            String linkUrl,
            Long referenceId
    );

    Page<NotificationResponse> getMyNotifications(UserPrincipal currentUser, Pageable pageable);

    UnreadCountResponse getUnreadCount(UserPrincipal currentUser);

    NotificationResponse markAsRead(UserPrincipal currentUser, Long notificationId);

    void markAllAsRead(UserPrincipal currentUser);
}
