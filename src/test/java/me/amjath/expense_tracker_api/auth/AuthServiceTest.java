package me.amjath.expense_tracker_api.auth;

import me.amjath.expense_tracker_api.auth.dto.LoginRequest;
import me.amjath.expense_tracker_api.auth.dto.LogoutRequest;
import me.amjath.expense_tracker_api.auth.dto.RegisterRequest;
import me.amjath.expense_tracker_api.auth.entity.RefreshToken;
import me.amjath.expense_tracker_api.auth.repository.RefreshTokenRepository;
import me.amjath.expense_tracker_api.auth.service.AuthService;
import me.amjath.expense_tracker_api.exception.BadRequestException;
import me.amjath.expense_tracker_api.exception.UnauthorizedException;
import me.amjath.expense_tracker_api.security.JwtProperties;
import me.amjath.expense_tracker_api.security.JwtService;
import me.amjath.expense_tracker_api.user.entity.Role;
import me.amjath.expense_tracker_api.user.entity.User;
import me.amjath.expense_tracker_api.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private JwtProperties jwtProperties;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("encoded_password");
        testUser.setRole(Role.USER);
    }

    // ---------------------------------------------------------------
    // Register tests
    // ---------------------------------------------------------------

    @Test
    @DisplayName("register - success")
    void register_success() {
        var request = new RegisterRequest();
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateAccessToken(any())).thenReturn("access_token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604800000L);
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(900000L);

        var response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("register - email already exists throws BadRequestException")
    void register_emailExists_throwsBadRequest() {
        var request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    // ---------------------------------------------------------------
    // Login tests
    // ---------------------------------------------------------------

    @Test
    @DisplayName("login - success")
    void login_success() {
        var request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(any())).thenReturn("access_token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604800000L);
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(900000L);

        var response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("login - bad credentials throws exception")
    void login_badCredentials_throwsException() {
        var request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    // ---------------------------------------------------------------
    // Logout tests
    // ---------------------------------------------------------------

    @Test
    @DisplayName("logout - single device revokes one token")
    void logout_singleDevice_revokesToken() {
        RefreshToken token = RefreshToken.builder()
                .id(UUID.randomUUID())
                .token("raw_token")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();

        var request = new LogoutRequest();
        request.setRefreshToken("raw_token");
        request.setLogoutAll(false);

        when(refreshTokenRepository.findByToken("raw_token")).thenReturn(Optional.of(token));

        authService.logout(request);

        assertThat(token.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(token);
    }

    @Test
    @DisplayName("logout - invalid token throws UnauthorizedException")
    void logout_invalidToken_throwsUnauthorized() {
        var request = new LogoutRequest();
        request.setRefreshToken("invalid_token");

        when(refreshTokenRepository.findByToken("invalid_token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.logout(request))
                .isInstanceOf(UnauthorizedException.class);
    }
}
