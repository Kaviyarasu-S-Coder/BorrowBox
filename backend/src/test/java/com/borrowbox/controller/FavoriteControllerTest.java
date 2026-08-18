package com.borrowbox.controller;

import com.borrowbox.dto.request.RegisterRequest;
import com.borrowbox.entity.Category;
import com.borrowbox.entity.Item;
import com.borrowbox.entity.ItemCondition;
import com.borrowbox.entity.ItemStatus;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private RatingRepository ratingRepository;

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

    private Long itemId;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        favoriteRepository.deleteAll();
        ratingRepository.deleteAll();
        notificationRepository.deleteAll();
        conditionRepository.deleteAll();
        transactionRepository.deleteAll();
        borrowRequestRepository.deleteAll();
        itemRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        Category cat = Category.builder()
                .name("Games")
                .slug("games")
                .icon("Gamepad")
                .isActive(true)
                .build();
        cat = categoryRepository.save(cat);

        RegisterRequest userReq = RegisterRequest.builder()
                .email("favuser@borrowbox.test")
                .password("Password123!")
                .fullName("Fav User")
                .build();
        MvcResult res = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andReturn();
        userToken = objectMapper.readTree(res.getResponse().getContentAsString()).get("data").get("accessToken").asText();
        Long userId = objectMapper.readTree(res.getResponse().getContentAsString()).get("data").get("user").get("id").asLong();

        Item item = Item.builder()
                .owner(userRepository.findById(userId).orElseThrow())
                .category(cat)
                .title("PlayStation 5 Console")
                .description("Next gen console with 2 DualSense controllers.")
                .condition(ItemCondition.LIKE_NEW)
                .depositAmount(BigDecimal.valueOf(5000))
                .dailyRate(BigDecimal.valueOf(400))
                .minBorrowDays(1)
                .maxBorrowDays(7)
                .location("Indiranagar, Bangalore")
                .status(ItemStatus.AVAILABLE)
                .build();
        item = itemRepository.save(item);
        itemId = item.getId();
    }

    @Test
    @DisplayName("Should toggle item in favorites watchlist")
    void testToggleFavoritesFlow() throws Exception {
        // 1. Toggle ON -> isFavorited = true
        mockMvc.perform(post("/api/favorites/" + itemId + "/toggle")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isFavorited", is(true)))
                .andExpect(jsonPath("$.data.totalFavorites", is(1)));

        // 2. Fetch User favorites list -> contains 1 item
        mockMvc.perform(get("/api/favorites")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title", is("PlayStation 5 Console")));

        // 3. Check status endpoint
        mockMvc.perform(get("/api/favorites/" + itemId + "/status")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isFavorited", is(true)));

        // 4. Toggle OFF -> isFavorited = false
        mockMvc.perform(post("/api/favorites/" + itemId + "/toggle")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isFavorited", is(false)))
                .andExpect(jsonPath("$.data.totalFavorites", is(0)));

        // 5. Fetch favorites list -> 0 items
        mockMvc.perform(get("/api/favorites")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(0)));
    }
}
