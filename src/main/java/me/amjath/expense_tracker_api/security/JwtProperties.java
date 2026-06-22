package me.amjath.expense_tracker_api.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT configuration properties bound from application.properties.
 * Prefix: application.jwt
 */
@Data
@Component
@ConfigurationProperties(prefix = "application.jwt")
public class JwtProperties {

    /** Base64-encoded secret key — must be at least 256 bits. */
    private String secret;

    /** Access token validity in milliseconds (default: 15 minutes). */
    private long accessTokenExpiration = 900_000L;

    /** Refresh token validity in milliseconds (default: 7 days). */
    private long refreshTokenExpiration = 604_800_000L;
}
