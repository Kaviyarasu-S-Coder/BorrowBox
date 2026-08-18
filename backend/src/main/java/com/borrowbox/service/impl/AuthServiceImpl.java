package com.borrowbox.service.impl;

import com.borrowbox.dto.request.LoginRequest;
import com.borrowbox.dto.request.RefreshTokenRequest;
import com.borrowbox.dto.request.RegisterRequest;
import com.borrowbox.dto.response.AuthResponse;
import com.borrowbox.dto.response.UserSummaryResponse;
import com.borrowbox.entity.Role;
import com.borrowbox.entity.User;
import com.borrowbox.exception.BadRequestException;
import com.borrowbox.exception.ConflictException;
import com.borrowbox.exception.ResourceNotFoundException;
import com.borrowbox.exception.UnauthorizedException;
import com.borrowbox.repository.UserRepository;
import com.borrowbox.security.JwtTokenProvider;
import com.borrowbox.security.UserPrincipal;
import com.borrowbox.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account with email '" + email + "' already exists.");
        }

        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);

        // Auto-assign ADMIN role if email is admin@borrowbox.com for demonstration
        if (email.equalsIgnoreCase("admin@borrowbox.com")) {
            roles.add(Role.ROLE_ADMIN);
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(request.getPhone())
                .location(request.getLocation() != null ? request.getLocation().trim() : "Local Area")
                .roles(roles)
                .isActive(true)
                .isVerified(false)
                .averageRating(5.0)
                .ratingCount(0)
                .reputationScore(80.0)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Successfully registered new user: ID={}, email={}", savedUser.getId(), savedUser.getEmail());

        UserPrincipal principal = UserPrincipal.create(savedUser);
        String accessToken = tokenProvider.generateAccessToken(principal);
        String refreshToken = tokenProvider.generateRefreshToken(principal);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .refreshToken(refreshToken)
                .user(mapToSummary(savedUser))
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String accessToken = tokenProvider.generateAccessToken(userPrincipal);
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal);

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        log.info("User successfully authenticated: ID={}, email={}", user.getId(), user.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .refreshToken(refreshToken)
                .user(mapToSummary(user))
                .build();
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();

        if (!tokenProvider.validateToken(token)) {
            throw new UnauthorizedException("Invalid or expired refresh token.");
        }

        String email = tokenProvider.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account has been deactivated.");
        }

        UserPrincipal principal = UserPrincipal.create(user);
        String newAccessToken = tokenProvider.generateAccessToken(principal);
        String newRefreshToken = tokenProvider.generateRefreshToken(principal);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .refreshToken(newRefreshToken)
                .user(mapToSummary(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryResponse getCurrentUser(UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("User is not authenticated.");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        return mapToSummary(user);
    }

    private UserSummaryResponse mapToSummary(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return UserSummaryResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .location(user.getLocation())
                .profileImageUrl(user.getProfileImageUrl())
                .isVerified(user.isVerified())
                .averageRating(user.getAverageRating())
                .ratingCount(user.getRatingCount())
                .reputationScore(user.getReputationScore())
                .roles(roleNames)
                .build();
    }
}
