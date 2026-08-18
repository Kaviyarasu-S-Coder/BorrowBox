package com.borrowbox.controller;

import com.borrowbox.BaseIntegrationTest;
import com.borrowbox.dto.request.RegisterRequest;
import com.borrowbox.entity.NotificationType;
import com.borrowbox.entity.User;
import com.borrowbox.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NotificationControllerTest extends BaseIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    private String userToken;
    private Long notificationId;

    @BeforeEach
    void setUpNotificationTest() throws Exception {
        RegisterRequest req1 = RegisterRequest.builder()
                .email("notif1@borrowbox.test")
                .password("Password123!")
                .fullName("Notif User 1")
                .build();
        MvcResult res1 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andReturn();
        userToken = objectMapper.readTree(res1.getResponse().getContentAsString()).get("data").get("accessToken").asText();
        User user1 = userRepository.findByEmail("notif1@borrowbox.test").orElseThrow();

        // Create 2 notifications
        notificationService.createNotification(
                user1,
                NotificationType.REQUEST_RECEIVED,
                "New Borrow Request",
                "You received a request for Camera",
                "/requests/1",
                1L
        );

        var n2 = notificationService.createNotification(
                user1,
                NotificationType.REQUEST_ACCEPTED,
                "Request Accepted!",
                "Your request for Camera was accepted.",
                "/transactions/1",
                1L
        );
        notificationId = n2.getId();
    }

    @Test
    @DisplayName("Should fetch notifications, unread count, and mark as read")
    void testNotificationFlow() throws Exception {
        // 1. Get unread count -> 2
        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount", is(2)));

        // 2. Fetch list
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)));

        // 3. Mark single as read
        mockMvc.perform(put("/api/notifications/" + notificationId + "/read")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isRead", is(true)));

        // 4. Verify unread count -> 1
        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount", is(1)));

        // 5. Mark all as read
        mockMvc.perform(put("/api/notifications/read-all")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // 6. Verify unread count -> 0
        mockMvc.perform(get("/api/notifications/unread-count")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount", is(0)));
    }
}
