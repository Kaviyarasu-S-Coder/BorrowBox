package com.borrowbox.service;

import com.borrowbox.dto.request.UpdateProfileRequest;
import com.borrowbox.dto.response.UserProfileResponse;
import com.borrowbox.security.UserPrincipal;

public interface UserService {

    UserProfileResponse getPublicProfile(Long userId);

    UserProfileResponse getMyProfile(UserPrincipal currentUser);

    UserProfileResponse updateProfile(UserPrincipal currentUser, UpdateProfileRequest request);
}
