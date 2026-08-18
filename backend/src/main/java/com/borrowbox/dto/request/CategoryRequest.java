package com.borrowbox.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    private String slug;

    private String description;

    private String icon; // e.g. "Camera", "Wrench", "Book", "Tv", "Tent"

    private Long parentId; // Optional parent category ID for creating subcategories

    @Builder.Default
    private boolean active = true;
}
