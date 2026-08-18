package com.borrowbox.controller;

import com.borrowbox.BaseIntegrationTest;
import com.borrowbox.dto.request.CreateItemRequest;
import com.borrowbox.dto.request.RegisterRequest;
import com.borrowbox.dto.request.UpdateItemRequest;
import com.borrowbox.entity.Category;
import com.borrowbox.entity.ItemCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ItemControllerTest extends BaseIntegrationTest {

    private Long categoryId;
    private String user1Token;
    private String user2Token;

    @BeforeEach
    void setUpItemTest() throws Exception {
        // 1. Create Category
        Category cat = Category.builder()
                .name("Tools")
                .slug("tools")
                .icon("Wrench")
                .isActive(true)
                .build();
        cat = categoryRepository.save(cat);
        categoryId = cat.getId();

        // 2. Register User 1
        RegisterRequest u1 = RegisterRequest.builder()
                .email("user1@borrowbox.test")
                .password("Password123!")
                .fullName("User One")
                .build();
        MvcResult res1 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(u1)))
                .andReturn();
        user1Token = objectMapper.readTree(res1.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        // 3. Register User 2
        RegisterRequest u2 = RegisterRequest.builder()
                .email("user2@borrowbox.test")
                .password("Password123!")
                .fullName("User Two")
                .build();
        MvcResult res2 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(u2)))
                .andReturn();
        user2Token = objectMapper.readTree(res2.getResponse().getContentAsString()).get("data").get("accessToken").asText();
    }

    @Test
    @DisplayName("Should successfully create and retrieve item listing")
    void testCreateAndGetItem() throws Exception {
        CreateItemRequest createReq = CreateItemRequest.builder()
                .title("Bosch Hammer Drill 500W")
                .categoryId(categoryId)
                .subCategory("Power Tools")
                .description("Powerful drill machine with masonry bits included.")
                .condition(ItemCondition.GOOD)
                .estimatedValue(BigDecimal.valueOf(4500))
                .depositAmount(BigDecimal.valueOf(500))
                .location("Indiranagar, Bangalore")
                .minBorrowDays(1)
                .maxBorrowDays(7)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title", is("Bosch Hammer Drill 500W")))
                .andExpect(jsonPath("$.data.categoryName", is("Tools")))
                .andReturn();

        Long itemId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // Get publicly
        mockMvc.perform(get("/api/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title", is("Bosch Hammer Drill 500W")))
                .andExpect(jsonPath("$.data.ownerName", is("User One")))
                .andExpect(jsonPath("$.data.viewCount", is(1)));
    }

    @Test
    @DisplayName("Should search items with query and category filter")
    void testSearchAndFilterItems() throws Exception {
        CreateItemRequest item1 = CreateItemRequest.builder()
                .title("Makita Cordless Impact Driver")
                .categoryId(categoryId)
                .description("High-torque impact driver.")
                .condition(ItemCondition.NEW)
                .depositAmount(BigDecimal.valueOf(1000))
                .location("Indiranagar, Bangalore")
                .build();

        mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item1)))
                .andExpect(status().isCreated());

        // Search by query "impact"
        mockMvc.perform(get("/api/items?query=impact"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title", is("Makita Cordless Impact Driver")));

        // Search by category slug "tools"
        mockMvc.perform(get("/api/items?categorySlug=tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)));

        // Discovery featured
        mockMvc.perform(get("/api/items/featured/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    @DisplayName("Should prevent User B from updating User A's item (IDOR protection)")
    void testIdorProtectionOnItemUpdate() throws Exception {
        CreateItemRequest createReq = CreateItemRequest.builder()
                .title("Tripod Stand 60 inch")
                .categoryId(categoryId)
                .description("Sturdy tripod for photography.")
                .condition(ItemCondition.LIKE_NEW)
                .location("Indiranagar, Bangalore")
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long itemId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asLong();

        // Attempt update using User 2's token
        UpdateItemRequest updateReq = UpdateItemRequest.builder()
                .title("Hacked Title")
                .categoryId(categoryId)
                .description("Unauthorized modification")
                .condition(ItemCondition.USED)
                .location("Hacked Location")
                .build();

        mockMvc.perform(put("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + user2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());
    }
}
