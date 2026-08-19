package com.borrowbox.controller;

import com.borrowbox.dto.request.*;
import com.borrowbox.dto.response.*;
import com.borrowbox.entity.*;
import com.borrowbox.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
public class BorrowBoxEndToEndFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    @Autowired
    private BorrowTransactionRepository borrowTransactionRepository;

    @Test
    @DisplayName("E2E Test: Full User Journey - Register -> List Item -> Check Dates -> Borrow -> OTP Handover -> Return -> Rate -> Reputation Recalc")
    void testCompleteBorrowLendLifecycleE2E() throws Exception {
        // Step 1: Register Owner (Alice)
        RegisterRequest ownerReg = RegisterRequest.builder()
                .email("alice_e2e@borrowbox.test")
                .password("Password123!")
                .fullName("Alice Owner")
                .location("Indiranagar, Bangalore")
                .build();

        MvcResult ownerRegRes = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerReg)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse ownerAuth = extractData(ownerRegRes, new TypeReference<>() {});
        String ownerToken = ownerAuth.getAccessToken();
        assertThat(ownerToken).isNotBlank();

        // Step 2: Register Borrower (Bob)
        RegisterRequest borrowerReg = RegisterRequest.builder()
                .email("bob_e2e@borrowbox.test")
                .password("Password123!")
                .fullName("Bob Borrower")
                .location("Koramangala, Bangalore")
                .build();

        MvcResult borrowerRegRes = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowerReg)))
                .andExpect(status().isCreated())
                .andReturn();

        AuthResponse borrowerAuth = extractData(borrowerRegRes, new TypeReference<>() {});
        String borrowerToken = borrowerAuth.getAccessToken();
        assertThat(borrowerToken).isNotBlank();

        // Step 3: Create Category
        Category category = categoryRepository.save(Category.builder()
                .name("Photography Equipment")
                .slug("photo-equip-e2e")
                .icon("Camera")
                .isActive(true)
                .build());

        // Step 4: Owner lists an item (Nikon Z6 II)
        CreateItemRequest createItemDto = CreateItemRequest.builder()
                .categoryId(category.getId())
                .subCategory("Mirrorless")
                .title("Nikon Z6 II 4K Full Frame Camera")
                .description("Professional Nikon Z6 II with 24-70mm f/4 S lens in mint condition.")
                .condition(ItemCondition.LIKE_NEW)
                .lendingMode("RATE_AND_DEPOSIT")
                .dailyRate(BigDecimal.valueOf(500.0))
                .depositAmount(BigDecimal.valueOf(3000.0))
                .estimatedValue(BigDecimal.valueOf(120000.0))
                .minBorrowDays(1)
                .maxBorrowDays(7)
                .location("Indiranagar, Bangalore")
                .imageUrls(List.of("https://images.unsplash.com/photo-1516035069371-29a1b244cc32"))
                .build();

        MvcResult itemRes = mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createItemDto)))
                .andExpect(status().isCreated())
                .andReturn();

        ItemResponse itemDetail = extractData(itemRes, new TypeReference<>() {});
        Long itemId = itemDetail.getId();
        assertThat(itemId).isNotNull();

        // Step 5: Borrower checks availability calendar
        LocalDate startDate = LocalDate.now().plusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(5);

        mockMvc.perform(get("/api/items/" + itemId + "/availability")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk());

        // Step 6: Borrower sends borrow request
        CreateBorrowRequestDto requestDto = CreateBorrowRequestDto.builder()
                .itemId(itemId)
                .startDate(startDate)
                .endDate(endDate)
                .message("Hi Alice, I need this camera for a weekend wildlife shoot.")
                .purpose("Wildlife shoot")
                .build();

        MvcResult reqRes = mockMvc.perform(post("/api/borrow-requests")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andReturn();

        BorrowRequestResponse borrowReq = extractData(reqRes, new TypeReference<>() {});
        Long requestId = borrowReq.getId();
        assertThat(borrowReq.getStatus()).isEqualTo(RequestStatus.PENDING);

        // Step 7: Owner accepts the borrow request
        RespondBorrowRequestDto acceptDto = RespondBorrowRequestDto.builder()
                .accept(true)
                .responseMessage("Approved! Let's meet at 10 AM.")
                .build();

        MvcResult acceptRes = mockMvc.perform(put("/api/borrow-requests/" + requestId + "/respond")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptDto)))
                .andExpect(status().isOk())
                .andReturn();

        BorrowRequestResponse acceptedReq = extractData(acceptRes, new TypeReference<>() {});
        assertThat(acceptedReq.getStatus()).isEqualTo(RequestStatus.ACCEPTED);

        // Step 8: Fetch transaction details & 6-digit OTP codes
        BorrowTransaction tx = borrowTransactionRepository.findByBorrowRequestId(requestId).orElseThrow();
        Long txId = tx.getId();
        String pickupCode = tx.getPickupCode();
        String returnCode = tx.getReturnCode();
        assertThat(pickupCode).hasSize(6);
        assertThat(returnCode).hasSize(6);

        // Step 9: Owner verifies Pickup Handover using Borrower's pickup code
        ConfirmCodeDto pickupDto = ConfirmCodeDto.builder()
                .verificationCode(pickupCode)
                .notes("Nikon Z6 II handed over in flawless condition with 2 batteries.")
                .build();

        MvcResult pickupRes = mockMvc.perform(post("/api/transactions/" + txId + "/pickup")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pickupDto)))
                .andExpect(status().isOk())
                .andReturn();

        TransactionResponse pickupTx = extractData(pickupRes, new TypeReference<>() {});
        assertThat(pickupTx.getStatus()).isEqualTo(TransactionStatus.BORROWED);

        // Step 10: Owner verifies Return Handover using Borrower's return code
        ConfirmCodeDto returnDto = ConfirmCodeDto.builder()
                .verificationCode(returnCode)
                .notes("Item returned clean and functional. Deposit released.")
                .build();

        MvcResult returnRes = mockMvc.perform(post("/api/transactions/" + txId + "/return")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(returnDto)))
                .andExpect(status().isOk())
                .andReturn();

        TransactionResponse completedTx = extractData(returnRes, new TypeReference<>() {});
        assertThat(completedTx.getStatus()).isEqualTo(TransactionStatus.COMPLETED);

        // Step 11: Borrower leaves 4D Rating for Owner
        CreateRatingDto ratingDto = CreateRatingDto.builder()
                .transactionId(txId)
                .rating(5)
                .communicationRating(5)
                .punctualityRating(5)
                .conditionRating(5)
                .review("Alice is an exceptional lender! Camera was immaculate.")
                .build();

        MvcResult ratingRes = mockMvc.perform(post("/api/ratings")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingDto)))
                .andExpect(status().isCreated())
                .andReturn();

        RatingResponse ratingResponse = extractData(ratingRes, new TypeReference<>() {});
        assertThat(ratingResponse.getRating()).isEqualTo(5);

        // Step 12: Verify Owner's Reputation Score and Profile
        User ownerUpdated = userRepository.findById(ownerAuth.getUser().getId()).orElseThrow();
        assertThat(ownerUpdated.getCompletedLendings()).isGreaterThanOrEqualTo(1);
        assertThat(ownerUpdated.getReputationScore()).isGreaterThanOrEqualTo(70.0);
    }

    private <T> T extractData(MvcResult result, TypeReference<ApiResponse<T>> typeReference) throws Exception {
        String json = result.getResponse().getContentAsString();
        ApiResponse<T> apiResponse = objectMapper.readValue(json, typeReference);
        return apiResponse.getData();
    }
}
