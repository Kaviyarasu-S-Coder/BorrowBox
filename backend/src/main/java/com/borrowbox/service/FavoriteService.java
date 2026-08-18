package com.borrowbox.service;

import com.borrowbox.dto.response.FavoriteStatusResponse;
import com.borrowbox.dto.response.ItemResponse;
import com.borrowbox.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteService {

    FavoriteStatusResponse toggleFavorite(UserPrincipal currentUser, Long itemId);

    Page<ItemResponse> getUserFavorites(UserPrincipal currentUser, Pageable pageable);

    FavoriteStatusResponse getFavoriteStatus(UserPrincipal currentUser, Long itemId);
}
