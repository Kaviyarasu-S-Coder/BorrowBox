package com.borrowbox.controller;

import com.borrowbox.dto.request.ConfirmCodeDto;
import com.borrowbox.dto.request.RecordConditionDto;
import com.borrowbox.dto.request.RegisterRequest;
import com.borrowbox.entity.*;
import com.borrowbox.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TransactionConditionRepository conditionRepository;

    @Autowired
    private BorrowTransactionRepository transactionRepository;

    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Long transactionId;
    private String ownerToken;
    private String borrowerToken;
    private String pickupCode = "654321";
    private String returnCode = "123456";

    @BeforeEach
    void setUp() throws Exception {
        notificationRepository.deleteAll();
        conditionRepository.deleteAll();
        transactionRepository.deleteAll();
        borrowRequestRepository.deleteAll();
        itemRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Category
        Category cat = Category.builder()
                .name("Electronics")
                .slug("electronics")
                .icon("Cpu")
                .isActive(true)
                .build();
        cat = categoryRepository.save(cat);

        // 2. Owner
        RegisterRequest ownerReq = RegisterRequest.builder()
                .email("txowner@borrowbox.test")
                .password("Password123!")
                .fullName("Tx Owner")
                .build();
        MvcResult res1 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerReq)))
                .andReturn();
        ownerToken = objectMapper.readTree(res1.getResponse().getContentAsString()).get("data").get("accessToken").asText();
        User owner = userRepository.findByEmail("txowner@borrowbox.test").orElseThrow();

        // 3. Borrower
        RegisterRequest borrowerReq = RegisterRequest.builder()
                .email("txborrower@borrowbox.test")
                .password("Password123!")
                .fullName("Tx Borrower")
                .build();
        MvcResult res2 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowerReq)))
                .andReturn();
        borrowerToken = objectMapper.readTree(res2.getResponse().getContentAsString()).get("data").get("accessToken").asText();
        User borrower = userRepository.findByEmail("txborrower@borrowbox.test").orElseThrow();

        // 4. Item
        Item item = Item.builder()
                .owner(owner)
                .category(cat)
                .title("Drone 4K Quadcopter")
                .description("High-end aerial photography drone.")
                .condition(ItemCondition.LIKE_NEW)
                .depositAmount(BigDecimal.valueOf(5000))
                .dailyRate(BigDecimal.valueOf(800))
                .minBorrowDays(1)
                .maxBorrowDays(7)
                .location("Indiranagar, Bangalore")
                .status(ItemStatus.AVAILABLE)
                .build();
        item = itemRepository.save(item);

        // 5. Request
        BorrowRequest req = BorrowRequest.builder()
                .item(item)
                .borrower(borrower)
                .owner(owner)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(4))
                .status(RequestStatus.ACCEPTED)
                .purpose("Landscape video")
                .message("Will fly carefully.")
                .build();
        req = borrowRequestRepository.save(req);

        // 6. Transaction
        BorrowTransaction tx = BorrowTransaction.builder()
                .borrowRequest(req)
                .item(item)
                .owner(owner)
                .borrower(borrower)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .pickupCode(pickupCode)
                .returnCode(returnCode)
                .depositHeld(item.getDepositAmount())
                .handoverLocation(item.getLocation())
                .status(TransactionStatus.UPCOMING)
                .build();
        tx = transactionRepository.save(tx);
        transactionId = tx.getId();
    }

    @Test
    @DisplayName("Should execute full pickup -> condition log -> return lifecycle")
    void testFullTransactionFlow() throws Exception {
        // 1. Attempt pickup with wrong code -> 400 Bad Request
        ConfirmCodeDto wrongCode = ConfirmCodeDto.builder()
                .verificationCode("000000")
                .build();
        mockMvc.perform(post("/api/transactions/" + transactionId + "/pickup")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongCode)))
                .andExpect(status().isBadRequest());

        // 2. Confirm pickup with correct code -> status BORROWED
        ConfirmCodeDto correctPickup = ConfirmCodeDto.builder()
                .verificationCode(pickupCode)
                .notes("Handed over drone with 2 batteries and charger.")
                .build();
        mockMvc.perform(post("/api/transactions/" + transactionId + "/pickup")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctPickup)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("BORROWED")))
                .andExpect(jsonPath("$.data.ownerPickupConfirmed", is(true)));

        // 3. Record pickup condition snapshot
        RecordConditionDto pickupCond = RecordConditionDto.builder()
                .stage(ConditionStage.PICKUP)
                .condition(ItemCondition.LIKE_NEW)
                .notes("Drone pristine, propellers intact.")
                .photoUrls(List.of("/uploads/drone_pickup_1.jpg"))
                .build();
        mockMvc.perform(post("/api/transactions/" + transactionId + "/condition")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pickupCond)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stage", is("PICKUP")));

        // 4. Confirm return with correct return code -> status COMPLETED
        ConfirmCodeDto correctReturn = ConfirmCodeDto.builder()
                .verificationCode(returnCode)
                .notes("Returned in perfect shape!")
                .build();
        mockMvc.perform(post("/api/transactions/" + transactionId + "/return")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctReturn)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("COMPLETED")))
                .andExpect(jsonPath("$.data.ownerReturnConfirmed", is(true)));

        // 5. Verify transaction state
        mockMvc.perform(get("/api/transactions/" + transactionId)
                        .header("Authorization", "Bearer " + borrowerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("COMPLETED")));
    }
}
