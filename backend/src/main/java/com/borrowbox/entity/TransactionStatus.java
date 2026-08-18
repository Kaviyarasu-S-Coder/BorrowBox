package com.borrowbox.entity;

public enum TransactionStatus {
    UPCOMING,
    READY_FOR_PICKUP,
    BORROWED,
    RETURN_PENDING,
    RETURNED,
    COMPLETED,
    OVERDUE,
    DISPUTED,
    CANCELLED
}
