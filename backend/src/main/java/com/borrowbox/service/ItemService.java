package com.borrowbox.service;

import com.borrowbox.dto.request.CreateItemRequest;
import com.borrowbox.dto.request.UpdateItemRequest;
import com.borrowbox.dto.response.ItemResponse;
import com.borrowbox.dto.response.ItemSummaryResponse;
import com.borrowbox.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ItemService {

    ItemResponse createItem(UserPrincipal currentUser, CreateItemRequest request);

    ItemResponse updateItem(UserPrincipal currentUser, Long itemId, UpdateItemRequest request);

    void deleteItem(UserPrincipal currentUser, Long itemId);

    ItemResponse getItemById(Long itemId);

    Page<ItemSummaryResponse> getMyItems(UserPrincipal currentUser, Pageable pageable);

    ItemResponse uploadItemImage(UserPrincipal currentUser, Long itemId, MultipartFile file, boolean isPrimary);

    void deleteItemImage(UserPrincipal currentUser, Long itemId, Long imageId);
}
