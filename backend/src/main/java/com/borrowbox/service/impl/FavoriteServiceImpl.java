package com.borrowbox.service.impl;

import com.borrowbox.dto.response.FavoriteStatusResponse;
import com.borrowbox.dto.response.ItemResponse;
import com.borrowbox.entity.Favorite;
import com.borrowbox.entity.Item;
import com.borrowbox.entity.User;
import com.borrowbox.exception.ResourceNotFoundException;
import com.borrowbox.exception.UnauthorizedException;
import com.borrowbox.repository.FavoriteRepository;
import com.borrowbox.repository.ItemRepository;
import com.borrowbox.repository.UserRepository;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.FavoriteService;
import com.borrowbox.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemService itemService;

    @Override
    @Transactional
    public FavoriteStatusResponse toggleFavorite(UserPrincipal currentUser, Long itemId) {
        if (currentUser == null) {
            throw new UnauthorizedException("User must be authenticated to favorite an item.");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemId));

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        Optional<Favorite> existing = favoriteRepository.findByUserIdAndItemId(user.getId(), item.getId());
        boolean isFavorited;
        String message;

        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            isFavorited = false;
            message = "Item removed from favorites";
            log.info("User ID={} removed item ID={} from favorites", user.getId(), item.getId());
        } else {
            Favorite favorite = Favorite.builder()
                    .user(user)
                    .item(item)
                    .build();
            favoriteRepository.save(favorite);
            isFavorited = true;
            message = "Item added to favorites";
            log.info("User ID={} added item ID={} to favorites", user.getId(), item.getId());
        }

        long totalFavorites = favoriteRepository.countByItemId(itemId);

        return FavoriteStatusResponse.builder()
                .itemId(itemId)
                .isFavorited(isFavorited)
                .totalFavorites(totalFavorites)
                .message(message)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ItemResponse> getUserFavorites(UserPrincipal currentUser, Pageable pageable) {
        if (currentUser == null) {
            throw new UnauthorizedException("User must be authenticated to view favorites.");
        }

        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(fav -> itemService.getItemById(fav.getItem().getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public FavoriteStatusResponse getFavoriteStatus(UserPrincipal currentUser, Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new ResourceNotFoundException("Item", "id", itemId);
        }

        boolean isFavorited = false;
        if (currentUser != null) {
            isFavorited = favoriteRepository.existsByUserIdAndItemId(currentUser.getId(), itemId);
        }

        long totalFavorites = favoriteRepository.countByItemId(itemId);

        return FavoriteStatusResponse.builder()
                .itemId(itemId)
                .isFavorited(isFavorited)
                .totalFavorites(totalFavorites)
                .message(isFavorited ? "Item is in your favorites" : "Item is not in your favorites")
                .build();
    }
}
