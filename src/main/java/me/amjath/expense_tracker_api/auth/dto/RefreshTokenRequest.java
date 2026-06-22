package me.amjath.expense_tracker_api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    /** Optional — helps identify which session to associate the new token with. */
    private String deviceInfo;
}
