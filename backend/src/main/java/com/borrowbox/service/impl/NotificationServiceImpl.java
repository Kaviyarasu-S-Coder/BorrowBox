package com.borrowbox.service.impl;

import com.borrowbox.dto.response.NotificationResponse;
import com.borrowbox.dto.response.UnreadCountResponse;
import com.borrowbox.entity.Notification;
import com.borrowbox.entity.NotificationType;
import com.borrowbox.entity.User;
import com.borrowbox.exception.ForbiddenException;
import com.borrowbox.exception.ResourceNotFoundException;
import com.borrowbox.repository.NotificationRepository;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.EmailService;
import com.borrowbox.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public NotificationResponse createNotification(
            User recipient,
            NotificationType type,
            String title,
            String message,
            String linkUrl,
            Long referenceId
    ) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .message(message)
                .linkUrl(linkUrl)
                .referenceId(referenceId)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Created notification ID={} for user ID={}: '{}'", saved.getId(), recipient.getId(), title);

        // Async email dispatch
        if (recipient.getEmail() != null) {
            emailService.sendSimpleEmail(recipient.getEmail(), "[BorrowBox] " + title, message);
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(UserPrincipal currentUser, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(UserPrincipal currentUser) {
        long count = notificationRepository.countByRecipientIdAndIsReadFalse(currentUser.getId());
        return new UnreadCountResponse(count);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UserPrincipal currentUser, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You do not have permission to modify this notification.");
        }

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void markAllAsRead(UserPrincipal currentUser) {
        notificationRepository.markAllAsRead(currentUser.getId());
        log.info("Marked all notifications as read for user ID={}", currentUser.getId());
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .linkUrl(n.getLinkUrl())
                .referenceId(n.getReferenceId())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
