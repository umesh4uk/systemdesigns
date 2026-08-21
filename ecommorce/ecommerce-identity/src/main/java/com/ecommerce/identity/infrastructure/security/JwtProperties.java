package com.ecommerce.identity.infrastructure.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Getter
@Setter
@Validated
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** Base64-encoded secret — minimum 256 bits for HS256. */
    @NotBlank
    private String secret;

    /** Access token validity in seconds. Default: 15 minutes. */
    @Positive
    private long accessTokenExpirySeconds = 900;

    /** Refresh token validity in seconds. Default: 7 days. */
    @Positive
    private long refreshTokenExpirySeconds = 604800;

    /** Token issuer claim. */
    private String issuer = "ecommerce-platform";
}
