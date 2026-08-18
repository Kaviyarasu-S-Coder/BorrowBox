package com.borrowbox.controller;

import com.borrowbox.BaseIntegrationTest;
import com.borrowbox.dto.request.CreateBorrowRequestDto;
import com.borrowbox.dto.request.RegisterRequest;
import com.borrowbox.dto.request.RespondBorrowRequestDto;
import com.borrowbox.entity.Category;
import com.borrowbox.entity.Item;
import com.borrowbox.entity.ItemCondition;
import com.borrowbox.entity.ItemStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BorrowRequestControllerTest extends BaseIntegrationTest {

    private Long itemId;
    private String ownerToken;
    private String borrowerToken;
    private String thirdPartyToken;

    @BeforeEach
    void setUpBorrowRequest() throws Exception {
        // 1. Create Category
        Category cat = Category.builder()
                .name("Outdoors")
                .slug("outdoors")
                .icon("Tent")
                .isActive(true)
                .build();
        cat = categoryRepository.save(cat);

        // 2. Register Owner
        RegisterRequest ownerReq = RegisterRequest.builder()
                .email("owner@borrowbox.test")
                .password("Password123!")
                .fullName("Owner User")
                .build();
        MvcResult res1 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerReq)))
                .andReturn();
        ownerToken = objectMapper.readTree(res1.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        // 3. Register Borrower
        RegisterRequest borrowerReq = RegisterRequest.builder()
                .email("borrower@borrowbox.test")
                .password("Password123!")
                .fullName("Borrower User")
                .build();
        MvcResult res2 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowerReq)))
                .andReturn();
        borrowerToken = objectMapper.readTree(res2.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        // 4. Register Third Party
        RegisterRequest thirdPartyReq = RegisterRequest.builder()
                .email("other@borrowbox.test")
                .password("Password123!")
                .fullName("Other User")
                .build();
        MvcResult res3 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(thirdPartyReq)))
                .andReturn();
        thirdPartyToken = objectMapper.readTree(res3.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        // 5. Create Item under Owner
        Long ownerId = userRepository.findByEmail("owner@borrowbox.test").orElseThrow().getId();
        Item item = Item.builder()
                .owner(userRepository.findById(ownerId).orElseThrow())
                .category(cat)
                .title("Camping Tent 4-Person")
                .description("Waterproof dome tent with rainfly.")
                .condition(ItemCondition.LIKE_NEW)
                .depositAmount(BigDecimal.valueOf(1000))
                .dailyRate(BigDecimal.valueOf(200))
                .minBorrowDays(1)
                .maxBorrowDays(10)
                .location("Indiranagar, Bangalore")
                .status(ItemStatus.AVAILABLE)
                .build();
        item = itemRepository.save(item);
        itemId = item.getId();
    }

    @Test
    @DisplayName("Should submit borrow request and allow owner to accept and generate transaction")
    void testBorrowRequestLifecycle() throws Exception {
        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now().plusDays(5);

        // 1. Borrower submits request
        CreateBorrowRequestDto requestDto = CreateBorrowRequestDto.builder()
                .itemId(itemId)
                .startDate(start)
                .endDate(end)
                .purpose("Weekend trekking trip to Coorg")
                .message("Hi! I'll take good care of the tent.")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/borrow-requests")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status", is("PENDING")))
                .andExpect(jsonPath("$.data.totalDays", is(4)))
                .andReturn();

        Long requestId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // 2. Third-party cannot accept (403 Forbidden)
        RespondBorrowRequestDto acceptDto = RespondBorrowRequestDto.builder()
                .accept(true)
                .responseMessage("Sure, pick it up on Friday evening!")
                .build();

        mockMvc.perform(put("/api/borrow-requests/" + requestId + "/respond")
                        .header("Authorization", "Bearer " + thirdPartyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptDto)))
                .andExpect(status().isForbidden());

        // 3. Owner accepts request -> 200 OK, status ACCEPTED, transaction created
        mockMvc.perform(put("/api/borrow-requests/" + requestId + "/respond")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("ACCEPTED")))
                .andExpect(jsonPath("$.data.transactionId", notNullValue()));
    }

    @Test
    @DisplayName("Should reject attempt by owner to borrow own item")
    void testCannotBorrowOwnItem() throws Exception {
        CreateBorrowRequestDto requestDto = CreateBorrowRequestDto.builder()
                .itemId(itemId)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
                .purpose("Self borrow")
                .message("Test message")
                .build();

        mockMvc.perform(post("/api/borrow-requests")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("You cannot borrow your own listed item")));
    }
}
