package me.amjath.expense_tracker_api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.amjath.expense_tracker_api.auth.controller.AuthController;
import me.amjath.expense_tracker_api.auth.dto.AuthResponse;
import me.amjath.expense_tracker_api.auth.dto.LoginRequest;
import me.amjath.expense_tracker_api.auth.dto.RegisterRequest;
import me.amjath.expense_tracker_api.auth.service.AuthService;
import me.amjath.expense_tracker_api.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private AuthService authService;

    private static final AuthResponse AUTH_RESPONSE = AuthResponse.builder()
            .userId(UUID.randomUUID())
            .email("user@example.com")
            .firstName("Test")
            .lastName("User")
            .role("USER")
            .accessToken("mock_access_token")
            .refreshToken("mock_refresh_token")
            .accessTokenExpiresIn(900000L)
            .build();

    @Test
    @DisplayName("POST /api/v1/auth/register - 201 with valid request")
    void register_validRequest_returns201() throws Exception {
        var request = new RegisterRequest();
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("user@example.com");
        request.setPassword("password123");

        when(authService.register(any())).thenReturn(AUTH_RESPONSE);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock_access_token"))
                .andExpect(jsonPath("$.data.email").value("user@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - 400 with invalid email")
    void register_invalidEmail_returns400() throws Exception {
        var request = new RegisterRequest();
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("not-an-email");
        request.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 200 with valid credentials")
    void login_validCredentials_returns200() throws Exception {
        var request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        when(authService.login(any())).thenReturn(AUTH_RESPONSE);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.refreshToken").value("mock_refresh_token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 400 when password missing")
    void login_missingPassword_returns400() throws Exception {
        var request = new LoginRequest();
        request.setEmail("user@example.com");
        // password missing

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
