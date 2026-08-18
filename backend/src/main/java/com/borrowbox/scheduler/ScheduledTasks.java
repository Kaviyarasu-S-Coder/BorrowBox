package com.borrowbox.scheduler;

import com.borrowbox.service.ScheduledJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final ScheduledJobService scheduledJobService;

    /**
     * Runs every hour on the hour to flag overdue transactions and expire stale pending requests.
     * Cron: 0 0 * * * *
     */
    @Scheduled(cron = "${borrowbox.jobs.overdue-cron:0 0 * * * *}")
    public void runOverdueAndExpirationTasks() {
        log.info("Scheduled task starting: Overdue and Expiration checks...");
        int overdue = scheduledJobService.checkAndProcessOverdueTransactions();
        int expired = scheduledJobService.expirePendingRequests();
        log.info("Scheduled task completed: {} overdue processed, {} requests expired.", overdue, expired);
    }

    /**
     * Runs daily at 8:00 AM to send upcoming return & pickup reminders.
     * Cron: 0 0 8 * * *
     */
    @Scheduled(cron = "${borrowbox.jobs.reminders-cron:0 0 8 * * *}")
    public void runDailyRemindersTask() {
        log.info("Scheduled task starting: Daily reminder dispatches...");
        int count = scheduledJobService.sendUpcomingReminders();
        log.info("Scheduled task completed: {} reminders dispatched.", count);
    }
}
