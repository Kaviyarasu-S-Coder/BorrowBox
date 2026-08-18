package com.borrowbox.controller;

import com.borrowbox.BaseIntegrationTest;
import com.borrowbox.dto.request.*;
import com.borrowbox.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DisputeAndReportControllerTest extends BaseIntegrationTest {

    private Long transactionId;
    private Long itemId;
    private Long borrowerId;
    private Long ownerId;
    private String borrowerToken;
    private String ownerToken;
    private String adminToken;

    @BeforeEach
    void setUpDisputeAndReport() throws Exception {
        // 1. Admin
        RegisterRequest adminReq = RegisterRequest.builder()
                .email("admin@borrowbox.com")
                .password("AdminPass123!")
                .fullName("System Admin")
                .build();
        MvcResult adminRes = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminReq)))
                .andReturn();
        adminToken = objectMapper.readTree(adminRes.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        // 2. Owner
        RegisterRequest ownerReq = RegisterRequest.builder()
                .email("dispowner@borrowbox.test")
                .password("Password123!")
                .fullName("Disp Owner")
                .build();
        MvcResult ownerRes = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerReq)))
                .andReturn();
        ownerToken = objectMapper.readTree(ownerRes.getResponse().getContentAsString()).get("data").get("accessToken").asText();
        ownerId = objectMapper.readTree(ownerRes.getResponse().getContentAsString()).get("data").get("user").get("id").asLong();
        User owner = userRepository.findById(ownerId).orElseThrow();

        // 3. Borrower
        RegisterRequest borrowerReq = RegisterRequest.builder()
                .email("dispborrower@borrowbox.test")
                .password("Password123!")
                .fullName("Disp Borrower")
                .build();
        MvcResult borrowerRes = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowerReq)))
                .andReturn();
        borrowerToken = objectMapper.readTree(borrowerRes.getResponse().getContentAsString()).get("data").get("accessToken").asText();
        borrowerId = objectMapper.readTree(borrowerRes.getResponse().getContentAsString()).get("data").get("user").get("id").asLong();
        User borrower = userRepository.findById(borrowerId).orElseThrow();

        // 4. Category & Item
        Category cat = Category.builder()
                .name("Tools")
                .slug("tools")
                .icon("Wrench")
                .isActive(true)
                .build();
        cat = categoryRepository.save(cat);

        Item item = Item.builder()
                .owner(owner)
                .category(cat)
                .title("Pressure Washer 2000 PSI")
                .description("Heavy duty pressure washer.")
                .condition(ItemCondition.LIKE_NEW)
                .depositAmount(BigDecimal.valueOf(3000))
                .dailyRate(BigDecimal.valueOf(400))
                .minBorrowDays(1)
                .maxBorrowDays(5)
                .location("Indiranagar, Bangalore")
                .status(ItemStatus.AVAILABLE)
                .build();
        item = itemRepository.save(item);
        itemId = item.getId();

        // 5. Request & Transaction
        BorrowRequest req = BorrowRequest.builder()
                .item(item)
                .borrower(borrower)
                .owner(owner)
                .startDate(LocalDate.now().minusDays(3))
                .endDate(LocalDate.now().minusDays(1))
                .status(RequestStatus.ACCEPTED)
                .purpose("Cleaning patio")
                .message("Hi, I need pressure washer for cleaning patio")
                .build();
        req = borrowRequestRepository.save(req);

        BorrowTransaction tx = BorrowTransaction.builder()
                .borrowRequest(req)
                .item(item)
                .owner(owner)
                .borrower(borrower)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .pickupCode("123123")
                .returnCode("321321")
                .depositHeld(item.getDepositAmount())
                .handoverLocation(item.getLocation())
                .status(TransactionStatus.BORROWED)
                .build();
        tx = transactionRepository.save(tx);
        transactionId = tx.getId();
    }

    @Test
    @DisplayName("Should file dispute and allow admin resolution with penalty calculation")
    void testDisputeLifecycle() throws Exception {
        // 1. Owner files dispute for damage
        CreateDisputeDto disputeDto = CreateDisputeDto.builder()
                .transactionId(transactionId)
                .reason("ITEM_DAMAGED")
                .description("The pressure washer hose nozzle was cracked and leaking.")
                .evidenceImages(List.of("/uploads/damage1.jpg"))
                .build();

        MvcResult dispRes = mockMvc.perform(post("/api/disputes")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(disputeDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status", is("OPEN")))
                .andExpect(jsonPath("$.data.reason", is("ITEM_DAMAGED")))
                .andReturn();

        Long disputeId = objectMapper.readTree(dispRes.getResponse().getContentAsString()).get("data").get("id").asLong();

        // 2. Admin resolves in favor of owner
        ResolveDisputeDto resolveDto = ResolveDisputeDto.builder()
                .status(DisputeStatus.RESOLVED)
                .adminDecision("Deduct 1000 INR from deposit to replace nozzle.")
                .resolutionNotes("Evidence clearly shows cracked nozzle upon return.")
                .build();

        mockMvc.perform(put("/api/disputes/admin/" + disputeId + "/resolve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolveDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("RESOLVED")))
                .andExpect(jsonPath("$.data.resolvedByName", is("System Admin")));
    }

    @Test
    @DisplayName("Should file report and allow admin to deactivate reported item")
    void testReportLifecycle() throws Exception {
        // 1. Borrower reports item as faked
        CreateReportDto reportDto = CreateReportDto.builder()
                .reportedItemId(itemId)
                .reason("MISLEADING_SPECIFICATIONS")
                .description("The pressure washer specs do not match the real machine.")
                .build();

        MvcResult repRes = mockMvc.perform(post("/api/reports")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reportDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status", is("OPEN")))
                .andReturn();

        Long reportId = objectMapper.readTree(repRes.getResponse().getContentAsString()).get("data").get("id").asLong();

        // 2. Admin resolves and deactivates item
        ResolveReportDto resolveDto = ResolveReportDto.builder()
                .status(ReportStatus.RESOLVED)
                .adminNotes("Confirmed item was misleading. Deactivated item.")
                .deactivateItem(true)
                .build();

        mockMvc.perform(put("/api/reports/admin/" + reportId + "/resolve")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolveDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("RESOLVED")));

        // Verify item is now INACTIVE
        Item item = itemRepository.findById(itemId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(ItemStatus.INACTIVE, item.getStatus());
    }
}
