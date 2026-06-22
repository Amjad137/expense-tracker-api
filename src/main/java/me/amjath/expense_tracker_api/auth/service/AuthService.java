package me.amjath.expense_tracker_api.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.amjath.expense_tracker_api.auth.dto.AuthResponse;
import me.amjath.expense_tracker_api.auth.dto.LoginRequest;
import me.amjath.expense_tracker_api.auth.dto.LogoutRequest;
import me.amjath.expense_tracker_api.auth.dto.RefreshTokenRequest;
import me.amjath.expense_tracker_api.auth.dto.RegisterRequest;
import me.amjath.expense_tracker_api.auth.entity.RefreshToken;
import me.amjath.expense_tracker_api.auth.repository.RefreshTokenRepository;
import me.amjath.expense_tracker_api.exception.BadRequestException;
import me.amjath.expense_tracker_api.exception.UnauthorizedException;
import me.amjath.expense_tracker_api.security.JwtProperties;
import me.amjath.expense_tracker_api.security.JwtService;
import me.amjath.expense_tracker_api.user.entity.Role;
import me.amjath.expense_tracker_api.user.entity.User;
import me.amjath.expense_tracker_api.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;

    // Register
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());

        return buildAuthResponse(user, request.getDeviceInfo());
    }

    // Login
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user, request.getDeviceInfo());
    }

    // Refresh token
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (!storedToken.isValid()) {
            throw new UnauthorizedException("Refresh token is expired or revoked");
        }

        // Revoke the old token (rotation strategy)
        storedToken.revoke();
        refreshTokenRepository.save(storedToken);

        User user = storedToken.getUser();
        log.info("Refreshing tokens for user: {}", user.getEmail());

        return buildAuthResponse(user, request.getDeviceInfo() != null
                ? request.getDeviceInfo()
                : storedToken.getDeviceInfo());
    }

    // Logout
    @Transactional
    public void logout(LogoutRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (request.isLogoutAll()) {
            refreshTokenRepository.revokeAllUserTokens(token.getUser(), LocalDateTime.now());
            log.info("All sessions revoked for user: {}", token.getUser().getEmail());
        } else {
            token.revoke();
            refreshTokenRepository.save(token);
            log.info("Session revoked for user: {}", token.getUser().getEmail());
        }
    }

    // Private helpers
    private AuthResponse buildAuthResponse(User user, String deviceInfo) {
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(rawRefreshToken)
                .deviceInfo(deviceInfo)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpiration() / 1000))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .accessTokenExpiresIn(jwtProperties.getAccessTokenExpiration())
                .build();
    }
}
