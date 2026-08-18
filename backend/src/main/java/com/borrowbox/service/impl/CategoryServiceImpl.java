package com.borrowbox.service.impl;

import com.borrowbox.dto.request.CategoryRequest;
import com.borrowbox.dto.response.CategoryResponse;
import com.borrowbox.entity.Category;
import com.borrowbox.exception.ConflictException;
import com.borrowbox.exception.ResourceNotFoundException;
import com.borrowbox.repository.CategoryRepository;
import com.borrowbox.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "categories", key = "'all_active'")
    public List<CategoryResponse> getAllActiveCategories() {
        List<Category> topLevelCategories = categoryRepository.findByParentCategoryIsNullAndIsActiveTrue();
        return topLevelCategories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));

        return mapToResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        return mapToResponse(category);
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse createCategory(CategoryRequest request) {
        String slug = generateSlug(request.getName(), request.getSlug());

        if (categoryRepository.existsBySlug(slug)) {
            throw new ConflictException("Category with slug '" + slug + "' already exists.");
        }

        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent Category", "id", request.getParentId()));
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .slug(slug)
                .description(request.getDescription())
                .icon(request.getIcon() != null ? request.getIcon().trim() : "Package")
                .parentCategory(parent)
                .isActive(request.isActive())
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Created category: ID={}, slug={}", saved.getId(), saved.getSlug());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        if (request.getIcon() != null) category.setIcon(request.getIcon().trim());
        category.setActive(request.isActive());

        Category updated = categoryRepository.save(category);
        log.info("Updated category: ID={}", updated.getId());

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Soft delete / de-activate
        category.setActive(false);
        categoryRepository.save(category);
        log.info("Deactivated category: ID={}", id);
    }

    private String generateSlug(String name, String customSlug) {
        if (customSlug != null && !customSlug.isBlank()) {
            return customSlug.toLowerCase(Locale.ROOT).trim().replaceAll("[^a-z0-9-]", "-");
        }
        return name.toLowerCase(Locale.ROOT).trim().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    private CategoryResponse mapToResponse(Category category) {
        List<CategoryResponse> subs = category.getSubCategories() != null
                ? category.getSubCategories().stream()
                .filter(Category::isActive)
                .map(this::mapToResponse)
                .collect(Collectors.toList())
                : List.of();

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .icon(category.getIcon())
                .parentId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .active(category.isActive())
                .subCategories(subs)
                .build();
    }
}
