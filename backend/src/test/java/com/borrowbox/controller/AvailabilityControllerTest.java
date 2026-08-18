package com.borrowbox.controller;

import com.borrowbox.entity.*;
import com.borrowbox.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class AvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        conditionRepository.deleteAll();
        transactionRepository.deleteAll();
        borrowRequestRepository.deleteAll();
        itemRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        User owner = User.builder()
                .email("owner@borrowbox.test")
                .password("Password123!")
                .fullName("Owner Test")
                .isActive(true)
                .build();
        owner = userRepository.save(owner);

        User borrower = User.builder()
                .email("borrower@borrowbox.test")
                .password("Password123!")
                .fullName("Borrower Test")
                .isActive(true)
                .build();
        borrower = userRepository.save(borrower);

        Category cat = Category.builder()
                .name("Cameras")
                .slug("cameras")
                .icon("Camera")
                .isActive(true)
                .build();
        cat = categoryRepository.save(cat);

        Item item = Item.builder()
                .owner(owner)
                .category(cat)
                .title("Sony Alpha A7 IV")
                .description("Professional full-frame mirrorless camera.")
                .condition(ItemCondition.LIKE_NEW)
                .estimatedValue(BigDecimal.valueOf(200000))
                .depositAmount(BigDecimal.valueOf(10000))
                .dailyRate(BigDecimal.valueOf(1500))
                .minBorrowDays(1)
                .maxBorrowDays(7)
                .location("Indiranagar, Bangalore")
                .status(ItemStatus.AVAILABLE)
                .build();
        item = itemRepository.save(item);
        itemId = item.getId();

        // Create an existing booking from +10 days to +14 days
        LocalDate existingStart = LocalDate.now().plusDays(10);
        LocalDate existingEnd = LocalDate.now().plusDays(14);

        BorrowRequest req = BorrowRequest.builder()
                .item(item)
                .borrower(borrower)
                .owner(owner)
                .startDate(existingStart)
                .endDate(existingEnd)
                .status(RequestStatus.ACCEPTED)
                .purpose("Wedding shoot")
                .message("Hi, I would love to borrow your camera for the shoot.")
                .build();
        req = borrowRequestRepository.save(req);

        BorrowTransaction tx = BorrowTransaction.builder()
                .borrowRequest(req)
                .item(item)
                .owner(owner)
                .borrower(borrower)
                .startDate(existingStart)
                .endDate(existingEnd)
                .status(TransactionStatus.UPCOMING)
                .build();
        transactionRepository.save(tx);
    }

    @Test
    @DisplayName("Should confirm availability for non-overlapping dates")
    void testAvailableDateRange() throws Exception {
        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now().plusDays(5);

        mockMvc.perform(get("/api/items/" + itemId + "/availability")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available", is(true)))
                .andExpect(jsonPath("$.data.totalDays", is(4)))
                .andExpect(jsonPath("$.data.estimatedRentalCost", is(6000.00)));
    }

    @Test
    @DisplayName("Should detect conflict for overlapping dates")
    void testOverlappingDateRange() throws Exception {
        // Overlaps with +10 to +14
        LocalDate start = LocalDate.now().plusDays(12);
        LocalDate end = LocalDate.now().plusDays(15);

        mockMvc.perform(get("/api/items/" + itemId + "/availability")
                        .param("startDate", start.toString())
                        .param("endDate", end.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available", is(false)))
                .andExpect(jsonPath("$.data.message", is("Item is already booked for all or part of the selected period.")));
    }

    @Test
    @DisplayName("Should retrieve calendar booked ranges")
    void testGetCalendarRanges() throws Exception {
        mockMvc.perform(get("/api/items/" + itemId + "/calendar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].reason", is("BOOKED")));
    }
}
