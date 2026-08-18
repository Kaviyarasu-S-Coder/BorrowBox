package com.borrowbox.controller;

import com.borrowbox.dto.request.RegisterRequest;
import com.borrowbox.entity.Notification;
import com.borrowbox.entity.NotificationType;
import com.borrowbox.entity.User;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class NotificationControllerTest {

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
    private UserRepository userRepository;

    private String user1Token;
    private String user2Token;
    private Long user1Id;
    private Long notificationId;

    @BeforeEach
    void setUp() throws Exception {
        notificationRepository.deleteAll();
        conditionRepository.deleteAll();
        transactionRepository.deleteAll();
        borrowRequestRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

        // 1. User 1
        RegisterRequest u1 = RegisterRequest.builder()
                .email("notif1@borrowbox.test")
                .password("Password123!")
                .fullName("Notif User One")
                .build();
        MvcResult res1 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(u1)))
                .andReturn();
        user1Token = objectMapper.readTree(res1.getResponse().getContentAsString()).get("data").get("accessToken").asText();
        user1Id = objectMapper.readTree(res1.getResponse().getContentAsString()).get("data").get("user").get("id").asLong();

        // 2. User 2
        RegisterRequest u2 = RegisterRequest.builder()
                .email("notif2@borrowbox.test")
                .password("Password123!")
                .fullName("Notif User Two")
                .build();
        MvcResult res2 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(u2)))
                .andReturn();
        user2Token = objectMapper.readTree(res2.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        // 3. Create Notification for User 1
        User user1 = userRepository.findById(user1Id).orElseThrow();
        Notification notif = Notification.builder()
                .recipient(user1)
                .type(NotificationType.REQUEST_RECEIVED)
                .title("New Borrow Request")
                .message("Someone wants to borrow your Drill!")
                .linkUrl("/borrow-requests/1")
                .isRead(false)
                .build();
        notif = notificationRepository.save(notif);
        notificationId = notif.getId();
    }

    @Test
    @DisplayName("Should retrieve notifications and badge unread count")
    void testGetNotificationsAndUnreadCount() throws Exception {
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title", is("New Borrow Request")));

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount", is(1)));
    }

    @Test
    @DisplayName("Should mark notification as read and prevent unauthorized access")
    void testMarkAsRead() throws Exception {
        // User 2 cannot mark User 1's notification as read (403 Forbidden)
        mockMvc.perform(put("/api/notifications/" + notificationId + "/read")
                        .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isForbidden());

        // User 1 marks as read
        mockMvc.perform(put("/api/notifications/" + notificationId + "/read")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.read", is(true)));

        // Unread count should now be 0
        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount", is(0)));
    }
}
