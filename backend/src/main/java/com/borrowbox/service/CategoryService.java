package com.borrowbox.service;

import com.borrowbox.dto.request.CategoryRequest;
import com.borrowbox.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {

    List<CategoryResponse> getAllActiveCategories();

    CategoryResponse getCategoryBySlug(String slug);

    CategoryResponse getCategoryById(Long id);

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);
}
