package com.borrowbox.controller;

import com.borrowbox.BaseIntegrationTest;
import com.borrowbox.dto.request.RegisterRequest;
import com.borrowbox.dto.request.UpdateProfileRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Should hide email and phone on public profile view")
    void testPublicProfilePrivacy() throws Exception {
        RegisterRequest registerReq = RegisterRequest.builder()
                .email("publictest@borrowbox.test")
                .password("Password123!")
                .fullName("Alice Wonderland")
                .phone("+91-9876543210")
                .location("Koramangala, Bangalore")
                .build();

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        Long userId = objectMapper.readTree(regResult.getResponse().getContentAsString()).get("data").get("user").get("id").asLong();

        // Access public profile without authentication
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName", is("Alice Wonderland")))
                .andExpect(jsonPath("$.data.location", is("Koramangala, Bangalore")))
                .andExpect(jsonPath("$.data.email").doesNotExist()) // Email hidden
                .andExpect(jsonPath("$.data.phone").doesNotExist()); // Phone hidden
    }

    @Test
    @DisplayName("Should update user profile when authenticated")
    void testUpdateProfile() throws Exception {
        RegisterRequest registerReq = RegisterRequest.builder()
                .email("updatetest@borrowbox.test")
                .password("Password123!")
                .fullName("Bob Builder")
                .build();

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String token = objectMapper.readTree(regResult.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        UpdateProfileRequest updateReq = UpdateProfileRequest.builder()
                .fullName("Bob The Builder")
                .bio("I lend power tools and gardening gear!")
                .phone("+91-9988776655")
                .location("HSR Layout, Bangalore")
                .build();

        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName", is("Bob The Builder")))
                .andExpect(jsonPath("$.data.bio", is("I lend power tools and gardening gear!")))
                .andExpect(jsonPath("$.data.phone", is("+91-9988776655")))
                .andExpect(jsonPath("$.data.location", is("HSR Layout, Bangalore")));
    }
}
