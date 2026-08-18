package com.borrowbox.controller;

import com.borrowbox.BaseIntegrationTest;
import com.borrowbox.dto.request.RegisterRequest;
import com.borrowbox.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AdminDashboardControllerTest extends BaseIntegrationTest {

    private String adminToken;
    private Long targetUserId;

    @BeforeEach
    void setUpAdminTest() throws Exception {
        // Admin
        RegisterRequest adminReq = RegisterRequest.builder()
                .email("admin@borrowbox.com")
                .password("AdminPass123!")
                .fullName("System Administrator")
                .build();
        MvcResult adminRes = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminReq)))
                .andReturn();
        adminToken = objectMapper.readTree(adminRes.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        // Target normal user
        RegisterRequest userReq = RegisterRequest.builder()
                .email("targetuser@borrowbox.test")
                .password("Password123!")
                .fullName("Target Member")
                .build();
        MvcResult userRes = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andReturn();
        targetUserId = objectMapper.readTree(userRes.getResponse().getContentAsString()).get("data").get("user").get("id").asLong();
    }

    @Test
    @DisplayName("Should retrieve platform metrics from admin dashboard")
    void testGetPlatformStats() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard/stats")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(2))
                .andExpect(jsonPath("$.data.activeUsers").value(2))
                .andExpect(jsonPath("$.data.openDisputes").value(0));
    }

    @Test
    @DisplayName("Should list users and allow admin to toggle active status and verification badge")
    void testUserModerationActions() throws Exception {
        // 1. List users
        mockMvc.perform(get("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2));

        // 2. Toggle active status -> suspended
        mockMvc.perform(put("/api/admin/users/" + targetUserId + "/toggle-status")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active", is(false)));

        User suspended = userRepository.findById(targetUserId).orElseThrow();
        assertFalse(suspended.isActive());

        // 3. Toggle verification badge -> verified
        mockMvc.perform(put("/api/admin/users/" + targetUserId + "/toggle-verify")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verified", is(true)));

        User verified = userRepository.findById(targetUserId).orElseThrow();
        assertTrue(verified.isVerified());
    }
}
