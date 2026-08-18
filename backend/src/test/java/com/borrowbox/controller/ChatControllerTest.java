package com.borrowbox.controller;

import com.borrowbox.BaseIntegrationTest;
import com.borrowbox.dto.request.RegisterRequest;
import com.borrowbox.dto.request.SendMessageDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ChatControllerTest extends BaseIntegrationTest {

    private Long user1Id;
    private Long user2Id;
    private Long user3Id;
    private String user1Token;
    private String user2Token;
    private String user3Token;

    @BeforeEach
    void setUpChatUsers() throws Exception {
        // 1. User 1
        RegisterRequest u1 = RegisterRequest.builder()
                .email("chat1@borrowbox.test")
                .password("Password123!")
                .fullName("Chat User 1")
                .build();
        MvcResult res1 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(u1)))
                .andReturn();
        user1Token = objectMapper.readTree(res1.getResponse().getContentAsString()).get("data").get("accessToken").asText();
        user1Id = objectMapper.readTree(res1.getResponse().getContentAsString()).get("data").get("user").get("id").asLong();

        // 2. User 2
        RegisterRequest u2 = RegisterRequest.builder()
                .email("chat2@borrowbox.test")
                .password("Password123!")
                .fullName("Chat User 2")
                .build();
        MvcResult res2 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(u2)))
                .andReturn();
        user2Token = objectMapper.readTree(res2.getResponse().getContentAsString()).get("data").get("accessToken").asText();
        user2Id = objectMapper.readTree(res2.getResponse().getContentAsString()).get("data").get("user").get("id").asLong();

        // 3. User 3
        RegisterRequest u3 = RegisterRequest.builder()
                .email("chat3@borrowbox.test")
                .password("Password123!")
                .fullName("Chat User 3")
                .build();
        MvcResult res3 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(u3)))
                .andReturn();
        user3Token = objectMapper.readTree(res3.getResponse().getContentAsString()).get("data").get("accessToken").asText();
        user3Id = objectMapper.readTree(res3.getResponse().getContentAsString()).get("data").get("user").get("id").asLong();
    }

    @Test
    @DisplayName("Should send chat message, list conversations, and mark as read")
    void testChatLifecycle() throws Exception {
        // 1. User 1 sends message to User 2
        SendMessageDto sendDto = SendMessageDto.builder()
                .recipientId(user2Id)
                .content("Hi, is your tripod available tomorrow?")
                .build();

        MvcResult msgResult = mockMvc.perform(post("/api/chat/messages")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sendDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.content", is("Hi, is your tripod available tomorrow?")))
                .andExpect(jsonPath("$.data.recipientId", is(user2Id.intValue())))
                .andReturn();

        Long convId = objectMapper.readTree(msgResult.getResponse().getContentAsString()).get("data").get("conversationId").asLong();

        // 2. User 2 checks conversations
        mockMvc.perform(get("/api/chat/conversations")
                        .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].otherUserName", is("Chat User 1")))
                .andExpect(jsonPath("$.data.content[0].lastMessage", is("Hi, is your tripod available tomorrow?")));

        // 3. User 2 reads messages in conversation
        mockMvc.perform(get("/api/chat/conversations/" + convId + "/messages")
                        .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)));

        // 4. User 2 marks conversation as read
        mockMvc.perform(put("/api/chat/conversations/" + convId + "/read")
                        .header("Authorization", "Bearer " + user2Token))
                .andExpect(status().isOk());

        // 5. User 3 (unauthorized outsider) attempts to read conversation messages -> 403 Forbidden
        mockMvc.perform(get("/api/chat/conversations/" + convId + "/messages")
                        .header("Authorization", "Bearer " + user3Token))
                .andExpect(status().isForbidden());
    }
}
