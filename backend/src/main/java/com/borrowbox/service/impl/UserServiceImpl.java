package com.borrowbox.service.impl;

import com.borrowbox.dto.request.UpdateProfileRequest;
import com.borrowbox.dto.response.UserProfileResponse;
import com.borrowbox.entity.User;
import com.borrowbox.exception.ResourceNotFoundException;
import com.borrowbox.exception.UnauthorizedException;
import com.borrowbox.repository.UserRepository;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getPublicProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return mapToProfile(user, false);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        return mapToProfile(user, true);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UserPrincipal currentUser, UpdateProfileRequest request) {
        if (currentUser == null) {
            throw new UnauthorizedException("User not authenticated");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        user.setFullName(request.getFullName().trim());
        if (request.getBio() != null) user.setBio(request.getBio().trim());
        if (request.getPhone() != null) user.setPhone(request.getPhone().trim());
        if (request.getLocation() != null) user.setLocation(request.getLocation().trim());
        if (request.getProfileImageUrl() != null) user.setProfileImageUrl(request.getProfileImageUrl().trim());

        User updatedUser = userRepository.save(user);
        log.info("Updated profile for user ID={}", updatedUser.getId());

        return mapToProfile(updatedUser, true);
    }

    private UserProfileResponse mapToProfile(User user, boolean isOwner) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return UserProfileResponse.builder()
                .id(user.getId())
                .email(isOwner ? user.getEmail() : null) // Protected
                .phone(isOwner ? user.getPhone() : null) // Protected
                .fullName(user.getFullName())
                .bio(user.getBio())
                .location(user.getLocation())
                .profileImageUrl(user.getProfileImageUrl())
                .isVerified(user.isVerified())
                .isActive(user.isActive())
                .averageRating(user.getAverageRating())
                .ratingCount(user.getRatingCount())
                .reputationScore(user.getReputationScore())
                .completedBorrowings(user.getCompletedBorrowings())
                .completedLendings(user.getCompletedLendings())
                .cancellationCount(user.getCancellationCount())
                .disputeCount(user.getDisputeCount())
                .roles(roleNames)
                .joinedDate(user.getCreatedAt())
                .build();
    }
}
