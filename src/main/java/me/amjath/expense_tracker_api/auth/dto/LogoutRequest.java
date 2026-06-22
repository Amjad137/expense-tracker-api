package me.amjath.expense_tracker_api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    /**
     * If true, revokes ALL sessions for this user (logout from all devices).
     * Default is false (logout from current device only).
     */
    private boolean logoutAll = false;
}
