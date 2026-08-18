package com.borrowbox.service;

import com.borrowbox.dto.request.CreateRatingDto;
import com.borrowbox.dto.response.RatingResponse;
import com.borrowbox.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RatingService {

    RatingResponse createRating(UserPrincipal currentUser, CreateRatingDto dto);

    Page<RatingResponse> getUserRatings(Long userId, Pageable pageable);
}
