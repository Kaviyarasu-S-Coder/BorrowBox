package com.borrowbox.service;

public interface ScheduledJobService {

    int checkAndProcessOverdueTransactions();

    int sendUpcomingReminders();

    int expirePendingRequests();
}
