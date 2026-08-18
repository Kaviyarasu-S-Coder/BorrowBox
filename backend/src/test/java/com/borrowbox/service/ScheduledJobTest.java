package com.borrowbox.service;

import com.borrowbox.BaseIntegrationTest;
import com.borrowbox.dto.request.RegisterRequest;
import com.borrowbox.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ScheduledJobTest extends BaseIntegrationTest {

    @Autowired
    private ScheduledJobService scheduledJobService;

    private User owner;
    private User borrower;
    private Item item;
    private String adminToken;

    @BeforeEach
    void setUpJobData() throws Exception {
        // Admin
        RegisterRequest adminReq = RegisterRequest.builder()
                .email("admin@borrowbox.com")
                .password("AdminPass123!")
                .fullName("Admin Job Tester")
                .build();
        MvcResult adminRes = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminReq)))
                .andReturn();
        adminToken = objectMapper.readTree(adminRes.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        // Owner & Borrower
        owner = userRepository.save(User.builder()
                .email("jobowner@borrowbox.test")
                .password("pass")
                .fullName("Job Owner")
                .build());

        borrower = userRepository.save(User.builder()
                .email("jobborrower@borrowbox.test")
                .password("pass")
                .fullName("Job Borrower")
                .build());

        Category cat = categoryRepository.save(Category.builder()
                .name("Outdoors")
                .slug("outdoors-job")
                .isActive(true)
                .build());

        item = itemRepository.save(Item.builder()
                .owner(owner)
                .category(cat)
                .title("Camping Tent 4-Person")
                .description("A spacious waterproof 4-person camping tent.")
                .condition(ItemCondition.LIKE_NEW)
                .location("Koramangala, Bangalore")
                .depositAmount(BigDecimal.valueOf(2000))
                .dailyRate(BigDecimal.valueOf(300))
                .status(ItemStatus.AVAILABLE)
                .build());
    }

    @Test
    @DisplayName("Should detect overdue transaction and transition to OVERDUE status")
    void testOverdueJobExecution() {
        BorrowRequest req = borrowRequestRepository.save(BorrowRequest.builder()
                .item(item)
                .borrower(borrower)
                .owner(owner)
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().minusDays(1))
                .purpose("Camping trip")
                .message("Need tent for weekend")
                .status(RequestStatus.ACCEPTED)
                .build());

        BorrowTransaction tx = transactionRepository.save(BorrowTransaction.builder()
                .borrowRequest(req)
                .item(item)
                .owner(owner)
                .borrower(borrower)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .pickupCode("111222")
                .returnCode("222333")
                .status(TransactionStatus.BORROWED)
                .build());

        int processed = scheduledJobService.checkAndProcessOverdueTransactions();
        assertEquals(1, processed);

        BorrowTransaction updatedTx = transactionRepository.findById(tx.getId()).orElseThrow();
        assertEquals(TransactionStatus.OVERDUE, updatedTx.getStatus());
    }

    @Test
    @DisplayName("Should expire pending request where start date has passed")
    void testExpiredRequestJobExecution() {
        BorrowRequest req = borrowRequestRepository.save(BorrowRequest.builder()
                .item(item)
                .borrower(borrower)
                .owner(owner)
                .startDate(LocalDate.now().minusDays(2))
                .endDate(LocalDate.now().plusDays(2))
                .purpose("Stale request")
                .message("Please accept my request")
                .status(RequestStatus.PENDING)
                .build());

        int expired = scheduledJobService.expirePendingRequests();
        assertEquals(1, expired);

        BorrowRequest updated = borrowRequestRepository.findById(req.getId()).orElseThrow();
        assertEquals(RequestStatus.EXPIRED, updated.getStatus());
    }

    @Test
    @DisplayName("Should allow admin to trigger background jobs via REST endpoints")
    void testAdminJobTriggers() throws Exception {
        mockMvc.perform(post("/api/admin/jobs/trigger-overdue")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.job").value("OVERDUE_DETECTION"));

        mockMvc.perform(post("/api/admin/jobs/trigger-reminders")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.job").value("UPCOMING_REMINDERS"));

        mockMvc.perform(post("/api/admin/jobs/trigger-expired-requests")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.job").value("REQUEST_EXPIRATION"));
    }
}
