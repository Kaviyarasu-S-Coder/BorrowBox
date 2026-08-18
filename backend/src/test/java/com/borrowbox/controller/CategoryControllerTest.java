package com.borrowbox.controller;

import com.borrowbox.BaseIntegrationTest;
import com.borrowbox.dto.request.CategoryRequest;
import com.borrowbox.dto.request.RegisterRequest;
import com.borrowbox.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CategoryControllerTest extends BaseIntegrationTest {

    @Test
    @DisplayName("Should retrieve active categories publicly")
    void testGetActiveCategories() throws Exception {
        Category cat1 = Category.builder()
                .name("Electronics")
                .slug("electronics")
                .icon("Cpu")
                .isActive(true)
                .build();
        categoryRepository.save(cat1);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is("Electronics")))
                .andExpect(jsonPath("$.data[0].slug", is("electronics")));
    }

    @Test
    @DisplayName("Should prevent non-admin from creating category and allow admin")
    void testCategoryAdminProtection() throws Exception {
        // 1. Register regular user
        RegisterRequest userReq = RegisterRequest.builder()
                .email("regular@borrowbox.test")
                .password("Password123!")
                .fullName("Regular User")
                .build();
        MvcResult userResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andReturn();
        String userToken = objectMapper.readTree(userResult.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        // 2. Register admin user (admin@borrowbox.com gets ROLE_ADMIN automatically)
        RegisterRequest adminReq = RegisterRequest.builder()
                .email("admin@borrowbox.com")
                .password("AdminSecret123!")
                .fullName("Super Admin")
                .build();
        MvcResult adminResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminReq)))
                .andReturn();
        String adminToken = objectMapper.readTree(adminResult.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        CategoryRequest newCategory = CategoryRequest.builder()
                .name("Musical Instruments")
                .icon("Music")
                .description("Guitars, Keyboards, Microphones")
                .build();

        // 3. Regular user should be forbidden (403)
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isForbidden());

        // 4. Admin should be authorized (201)
        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCategory)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name", is("Musical Instruments")))
                .andExpect(jsonPath("$.data.slug", is("musical-instruments")));
    }
}
