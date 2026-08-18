package com.borrowbox.service;

import com.borrowbox.dto.request.LoginRequest;
import com.borrowbox.dto.request.RefreshTokenRequest;
import com.borrowbox.dto.request.RegisterRequest;
import com.borrowbox.dto.response.AuthResponse;
import com.borrowbox.dto.response.UserSummaryResponse;
import com.borrowbox.security.UserPrincipal;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    UserSummaryResponse getCurrentUser(UserPrincipal currentUser);
}
