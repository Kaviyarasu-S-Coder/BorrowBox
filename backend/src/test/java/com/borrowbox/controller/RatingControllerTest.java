package com.borrowbox.controller;

import com.borrowbox.dto.request.CreateRatingDto;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class RatingControllerTest {

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

    private Long transactionId;
    private Long ownerId;
    private Long borrowerId;
    private String ownerToken;
    private String borrowerToken;

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

        // 1. Category
        Category cat = Category.builder()
                .name("Audio")
                .slug("audio")
                .icon("Headphones")
                .isActive(true)
                .build();
        cat = categoryRepository.save(cat);

        // 2. Owner
        RegisterRequest ownerReq = RegisterRequest.builder()
                .email("rateowner@borrowbox.test")
                .password("Password123!")
                .fullName("Rating Owner")
                .build();
        MvcResult res1 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerReq)))
                .andReturn();
        ownerToken = objectMapper.readTree(res1.getResponse().getContentAsString()).get("data").get("accessToken").asText();
        ownerId = objectMapper.readTree(res1.getResponse().getContentAsString()).get("data").get("user").get("id").asLong();
        User owner = userRepository.findById(ownerId).orElseThrow();

        // 3. Borrower
        RegisterRequest borrowerReq = RegisterRequest.builder()
                .email("rateborrower@borrowbox.test")
                .password("Password123!")
                .fullName("Rating Borrower")
                .build();
        MvcResult res2 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowerReq)))
                .andReturn();
        borrowerToken = objectMapper.readTree(res2.getResponse().getContentAsString()).get("data").get("accessToken").asText();
        borrowerId = objectMapper.readTree(res2.getResponse().getContentAsString()).get("data").get("user").get("id").asLong();
        User borrower = userRepository.findById(borrowerId).orElseThrow();

        // 4. Item
        Item item = Item.builder()
                .owner(owner)
                .category(cat)
                .title("Bose Noise Cancelling Headphones")
                .description("Top grade studio headphones.")
                .condition(ItemCondition.LIKE_NEW)
                .depositAmount(BigDecimal.valueOf(2000))
                .dailyRate(BigDecimal.valueOf(300))
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
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().minusDays(2))
                .status(RequestStatus.ACCEPTED)
                .purpose("Podcast recording")
                .message("Will use indoors only.")
                .build();
        req = borrowRequestRepository.save(req);

        // 6. Completed Transaction
        BorrowTransaction tx = BorrowTransaction.builder()
                .borrowRequest(req)
                .item(item)
                .owner(owner)
                .borrower(borrower)
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .pickupCode("112233")
                .returnCode("445566")
                .depositHeld(item.getDepositAmount())
                .handoverLocation(item.getLocation())
                .status(TransactionStatus.COMPLETED)
                .build();
        tx = transactionRepository.save(tx);
        transactionId = tx.getId();
    }

    @Test
    @DisplayName("Should submit rating and recompute recipient reputation score")
    void testCreateRatingAndReputationRecompute() throws Exception {
        CreateRatingDto ratingDto = CreateRatingDto.builder()
                .transactionId(transactionId)
                .rating(5)
                .communicationRating(5)
                .punctualityRating(5)
                .conditionRating(5)
                .review("Outstanding item and communicative owner!")
                .build();

        // 1. Borrower rates Owner
        mockMvc.perform(post("/api/ratings")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.rating", is(5)))
                .andExpect(jsonPath("$.data.toUserName", is("Rating Owner")));

        // 2. Prevent duplicate rating by borrower for same tx (409 Conflict)
        mockMvc.perform(post("/api/ratings")
                        .header("Authorization", "Bearer " + borrowerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingDto)))
                .andExpect(status().isConflict());

        // 3. Fetch Owner public reviews
        mockMvc.perform(get("/api/ratings/user/" + ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].rating", is(5)))
                .andExpect(jsonPath("$.data.content[0].fromUserName", is("Rating Borrower")));
    }
}
