package com.ashishqa.api.authentication;

import com.ashishqa.api.config.ConfigurationException;

import java.util.Locale;

/**
 * Authentication mechanism selected by {@code auth.type}.
 * {@link AuthStrategies} maps this value to a concrete {@link AuthStrategy}.
 */
public enum AuthType {

    NONE,
    BASIC,
    BEARER,
    API_KEY,
    COOKIE_TOKEN;

    public static AuthType from(String raw) {
        if (raw == null || raw.isBlank()) {
            return NONE;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT).replace("-", "").replace("_", "");
        return switch (normalized) {
            case "none", "noauth" -> NONE;
            case "basic" -> BASIC;
            case "bearer" -> BEARER;
            case "apikey" -> API_KEY;
            case "cookietoken" -> COOKIE_TOKEN;
            default -> throw new ConfigurationException(
                    "Unknown auth.type '" + raw + "'. Supported: none, basic, bearer, apiKey, cookieToken");
        };
    }
}
