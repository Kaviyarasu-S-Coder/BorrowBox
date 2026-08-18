package com.borrowbox.service.impl;

import com.borrowbox.dto.request.CreateItemRequest;
import com.borrowbox.dto.request.UpdateItemRequest;
import com.borrowbox.dto.response.ItemImageResponse;
import com.borrowbox.dto.response.ItemResponse;
import com.borrowbox.dto.response.ItemSummaryResponse;
import com.borrowbox.entity.*;
import com.borrowbox.exception.BadRequestException;
import com.borrowbox.exception.ForbiddenException;
import com.borrowbox.exception.ResourceNotFoundException;
import com.borrowbox.exception.UnauthorizedException;
import com.borrowbox.repository.*;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.FileStorageService;
import com.borrowbox.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BorrowTransactionRepository transactionRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    @CacheEvict(value = {"items", "categories"}, allEntries = true)
    public ItemResponse createItem(UserPrincipal currentUser, CreateItemRequest request) {
        if (currentUser == null) {
            throw new UnauthorizedException("User must be authenticated to list items.");
        }

        User owner = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Item item = Item.builder()
                .owner(owner)
                .category(category)
                .subCategory(request.getSubCategory())
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .condition(request.getCondition())
                .estimatedValue(request.getEstimatedValue())
                .depositAmount(request.getDepositAmount())
                .dailyRate(request.getDailyRate())
                .lendingMode(request.getLendingMode())
                .location(request.getLocation() != null ? request.getLocation().trim() : owner.getLocation())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .minBorrowDays(request.getMinBorrowDays() != null ? request.getMinBorrowDays() : 1)
                .maxBorrowDays(request.getMaxBorrowDays() != null ? request.getMaxBorrowDays() : 14)
                .borrowingRules(request.getBorrowingRules())
                .status(ItemStatus.AVAILABLE)
                .borrowCount(0)
                .viewCount(0)
                .build();

        Item savedItem = itemRepository.save(item);

        // Add initial images if provided
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                ItemImage image = ItemImage.builder()
                        .item(savedItem)
                        .imageUrl(request.getImageUrls().get(i))
                        .isPrimary(i == 0)
                        .displayOrder(i)
                        .build();
                savedItem.addImage(image);
            }
            savedItem = itemRepository.save(savedItem);
        }

        log.info("Created item listing: ID={}, title='{}' by owner ID={}", savedItem.getId(), savedItem.getTitle(), owner.getId());
        return mapToResponse(savedItem);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"items", "categories"}, allEntries = true)
    public ItemResponse updateItem(UserPrincipal currentUser, Long itemId, UpdateItemRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

        validateOwnershipOrAdmin(currentUser, item);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        item.setTitle(request.getTitle().trim());
        item.setCategory(category);
        item.setSubCategory(request.getSubCategory());
        item.setDescription(request.getDescription().trim());
        item.setCondition(request.getCondition());
        if (request.getEstimatedValue() != null) item.setEstimatedValue(request.getEstimatedValue());
        if (request.getDepositAmount() != null) item.setDepositAmount(request.getDepositAmount());
        if (request.getDailyRate() != null) item.setDailyRate(request.getDailyRate());
        if (request.getLendingMode() != null) item.setLendingMode(request.getLendingMode());
        if (request.getLocation() != null) item.setLocation(request.getLocation().trim());
        if (request.getLatitude() != null) item.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) item.setLongitude(request.getLongitude());
        if (request.getMinBorrowDays() != null) item.setMinBorrowDays(request.getMinBorrowDays());
        if (request.getMaxBorrowDays() != null) item.setMaxBorrowDays(request.getMaxBorrowDays());
        if (request.getBorrowingRules() != null) item.setBorrowingRules(request.getBorrowingRules());
        if (request.getStatus() != null) item.setStatus(request.getStatus());

        Item updated = itemRepository.save(item);
        log.info("Updated item ID={}", updated.getId());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"items", "categories"}, allEntries = true)
    public void deleteItem(UserPrincipal currentUser, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

        validateOwnershipOrAdmin(currentUser, item);

        // Check if there are active transactions for this item
        List<TransactionStatus> activeStatuses = Arrays.asList(
                TransactionStatus.UPCOMING,
                TransactionStatus.READY_FOR_PICKUP,
                TransactionStatus.BORROWED,
                TransactionStatus.RETURN_PENDING
        );
        List<BorrowTransaction> activeTx = transactionRepository.findByItemIdAndStatusIn(itemId, activeStatuses);

        if (!activeTx.isEmpty()) {
            throw new BadRequestException("Cannot delete item with active or upcoming borrow bookings. Please complete or cancel bookings first.");
        }

        // Soft delete / mark inactive
        item.setStatus(ItemStatus.INACTIVE);
        itemRepository.save(item);
        log.info("Deactivated item ID={}", itemId);
    }

    @Override
    @Transactional
    public ItemResponse getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

        // Increment view count asynchronously/in transaction
        item.setViewCount(item.getViewCount() + 1);
        itemRepository.save(item);

        return mapToResponse(item);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemSummaryResponse> getMyItems(UserPrincipal currentUser, Pageable pageable) {
        if (currentUser == null) {
            throw new UnauthorizedException("User not authenticated.");
        }

        return itemRepository.findByOwnerId(currentUser.getId(), pageable)
                .map(this::mapToSummary);
    }

    @Override
    @Transactional
    public ItemResponse uploadItemImage(UserPrincipal currentUser, Long itemId, MultipartFile file, boolean isPrimary) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

        validateOwnershipOrAdmin(currentUser, item);

        String storedUrl = fileStorageService.storeFile(file);

        if (isPrimary) {
            item.getImages().forEach(img -> img.setPrimary(false));
        }

        ItemImage newImage = ItemImage.builder()
                .item(item)
                .imageUrl(storedUrl)
                .isPrimary(isPrimary || item.getImages().isEmpty())
                .displayOrder(item.getImages().size())
                .build();

        item.addImage(newImage);
        Item updated = itemRepository.save(item);

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteItemImage(UserPrincipal currentUser, Long itemId, Long imageId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

        validateOwnershipOrAdmin(currentUser, item);

        ItemImage image = itemImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ItemImage", "id", imageId));

        if (!image.getItem().getId().equals(itemId)) {
            throw new BadRequestException("Image does not belong to this item.");
        }

        fileStorageService.deleteFile(image.getImageUrl());
        item.getImages().remove(image);
        itemImageRepository.delete(image);

        // If removed image was primary and others exist, set first remaining as primary
        if (image.isPrimary() && !item.getImages().isEmpty()) {
            item.getImages().get(0).setPrimary(true);
        }

        itemRepository.save(item);
    }

    private void validateOwnershipOrAdmin(UserPrincipal currentUser, Item item) {
        if (currentUser == null) {
            throw new UnauthorizedException("Authentication required.");
        }
        boolean isOwner = item.getOwner().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("You do not have permission to modify this item.");
        }
    }

    private ItemResponse mapToResponse(Item item) {
        User owner = item.getOwner();
        Category cat = item.getCategory();

        List<ItemImageResponse> imageResponses = item.getImages().stream()
                .map(img -> ItemImageResponse.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .isPrimary(img.isPrimary())
                        .displayOrder(img.getDisplayOrder())
                        .build())
                .collect(Collectors.toList());

        return ItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .categoryId(cat != null ? cat.getId() : null)
                .categoryName(cat != null ? cat.getName() : null)
                .categorySlug(cat != null ? cat.getSlug() : null)
                .subCategory(item.getSubCategory())
                .condition(item.getCondition())
                .estimatedValue(item.getEstimatedValue())
                .depositAmount(item.getDepositAmount())
                .dailyRate(item.getDailyRate())
                .lendingMode(item.getLendingMode())
                .location(item.getLocation())
                .latitude(item.getLatitude())
                .longitude(item.getLongitude())
                .status(item.getStatus())
                .minBorrowDays(item.getMinBorrowDays())
                .maxBorrowDays(item.getMaxBorrowDays())
                .borrowingRules(item.getBorrowingRules())
                .images(imageResponses)
                .ownerId(owner.getId())
                .ownerName(owner.getFullName())
                .ownerProfileImage(owner.getProfileImageUrl())
                .ownerLocation(owner.getLocation())
                .ownerRating(owner.getAverageRating())
                .ownerRatingCount(owner.getRatingCount())
                .ownerReputation(owner.getReputationScore())
                .ownerCompletedLendings(owner.getCompletedLendings())
                .ownerJoinedDate(owner.getCreatedAt())
                .borrowCount(item.getBorrowCount())
                .viewCount(item.getViewCount())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private ItemSummaryResponse mapToSummary(Item item) {
        User owner = item.getOwner();
        Category cat = item.getCategory();

        String primaryImg = item.getImages().stream()
                .filter(ItemImage::isPrimary)
                .findFirst()
                .map(ItemImage::getImageUrl)
                .orElseGet(() -> !item.getImages().isEmpty() ? item.getImages().get(0).getImageUrl() : null);

        return ItemSummaryResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .categoryName(cat != null ? cat.getName() : null)
                .categorySlug(cat != null ? cat.getSlug() : null)
                .subCategory(item.getSubCategory())
                .condition(item.getCondition())
                .estimatedValue(item.getEstimatedValue())
                .depositAmount(item.getDepositAmount())
                .dailyRate(item.getDailyRate())
                .lendingMode(item.getLendingMode())
                .location(item.getLocation())
                .status(item.getStatus())
                .primaryImageUrl(primaryImg)
                .ownerId(owner.getId())
                .ownerName(owner.getFullName())
                .ownerRating(owner.getAverageRating())
                .ownerReputation(owner.getReputationScore())
                .borrowCount(item.getBorrowCount())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
