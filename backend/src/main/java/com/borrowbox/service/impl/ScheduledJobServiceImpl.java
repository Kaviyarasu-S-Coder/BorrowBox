package com.borrowbox.service.impl;

import com.borrowbox.entity.*;
import com.borrowbox.repository.BorrowRequestRepository;
import com.borrowbox.repository.BorrowTransactionRepository;
import com.borrowbox.service.EmailService;
import com.borrowbox.service.NotificationService;
import com.borrowbox.service.ScheduledJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledJobServiceImpl implements ScheduledJobService {

    private final BorrowTransactionRepository transactionRepository;
    private final BorrowRequestRepository borrowRequestRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Override
    @Transactional
    public int checkAndProcessOverdueTransactions() {
        LocalDate today = LocalDate.now();
        List<BorrowTransaction> overdueTxList = transactionRepository.findTransactionsDueForOverdue(today);

        log.info("Scheduled Job: Found {} overdue transactions to process.", overdueTxList.size());

        for (BorrowTransaction tx : overdueTxList) {
            tx.setStatus(TransactionStatus.OVERDUE);
            transactionRepository.save(tx);

            // Notify borrower
            notificationService.createNotification(
                    tx.getBorrower(),
                    NotificationType.OVERDUE,
                    "URGENT: Item Overdue for Return",
                    "Your borrow period for '" + tx.getItem().getTitle() + "' ended on " + tx.getEndDate() + ". Please arrange return immediately to avoid penalties.",
                    "/transactions/" + tx.getId(),
                    tx.getId()
            );

            // Notify owner
            notificationService.createNotification(
                    tx.getOwner(),
                    NotificationType.OVERDUE,
                    "Transaction #" + tx.getId() + " is Overdue",
                    "The borrower has not completed return for '" + tx.getItem().getTitle() + "' which was due on " + tx.getEndDate() + ".",
                    "/transactions/" + tx.getId(),
                    tx.getId()
            );

            // Send async email reminder
            emailService.sendSimpleEmail(
                    tx.getBorrower().getEmail(),
                    "URGENT: Item Overdue for Return - BorrowBox",
                    "Hello " + tx.getBorrower().getFullName() + ",\n\nYour borrow period for '" + tx.getItem().getTitle() + "' ended on " + tx.getEndDate() + ". Please arrange return immediately to avoid penalties."
            );
        }

        return overdueTxList.size();
    }

    @Override
    @Transactional
    public int sendUpcomingReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<BorrowTransaction> returnReminders = transactionRepository.findTransactionsDueForReminder(tomorrow);

        log.info("Scheduled Job: Sending {} return reminders for tomorrow ({}).", returnReminders.size(), tomorrow);

        for (BorrowTransaction tx : returnReminders) {
            notificationService.createNotification(
                    tx.getBorrower(),
                    NotificationType.RETURN_REMINDER,
                    "Reminder: Item Return Tomorrow",
                    "Your borrow period for '" + tx.getItem().getTitle() + "' ends tomorrow (" + tx.getEndDate() + "). Return OTP: " + tx.getReturnCode(),
                    "/transactions/" + tx.getId(),
                    tx.getId()
            );

            emailService.sendSimpleEmail(
                    tx.getBorrower().getEmail(),
                    "Reminder: Item Return Due Tomorrow - BorrowBox",
                    "Hello " + tx.getBorrower().getFullName() + ",\n\nYour borrow period for '" + tx.getItem().getTitle() + "' ends tomorrow (" + tx.getEndDate() + "). Your 6-digit return handover OTP is " + tx.getReturnCode() + "."
            );
        }

        return returnReminders.size();
    }

    @Override
    @Transactional
    public int expirePendingRequests() {
        LocalDate today = LocalDate.now();
        List<BorrowRequest> expiredList = borrowRequestRepository.findExpiredPendingRequests(today);

        log.info("Scheduled Job: Expiring {} stale pending requests.", expiredList.size());

        for (BorrowRequest req : expiredList) {
            req.setStatus(RequestStatus.EXPIRED);
            borrowRequestRepository.save(req);

            notificationService.createNotification(
                    req.getBorrower(),
                    NotificationType.REQUEST_REJECTED,
                    "Borrow Request Expired",
                    "Your request for '" + req.getItem().getTitle() + "' expired as the requested start date (" + req.getStartDate() + ") has passed.",
                    "/items/" + req.getItem().getId(),
                    req.getId()
            );
        }

        return expiredList.size();
    }
}
