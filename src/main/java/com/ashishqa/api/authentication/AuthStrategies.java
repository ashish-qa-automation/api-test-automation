package com.ashishqa.api.authentication;

import com.ashishqa.api.config.ConfigurationException;
import com.ashishqa.api.config.FrameworkConfig;

import java.util.Objects;

public final class AuthStrategies {

    private AuthStrategies() {
    }

    public static AuthStrategy from(FrameworkConfig config, TokenProvider tokenProvider) {
        Objects.requireNonNull(config, "config");
        return switch (config.authType()) {
            case NONE -> NoAuth.INSTANCE;
            case BASIC -> new BasicAuth(require(config.authUsername(), "auth.username"), config.authPassword());
            case BEARER -> new BearerTokenAuth(requireToken(tokenProvider, config.authType()));
            case API_KEY -> new ApiKeyAuth(config.apiKeyHeader(), require(config.apiKey(), "auth.apiKey"));
            case COOKIE_TOKEN -> new CookieTokenAuth(requireToken(tokenProvider, config.authType()));
        };
    }

    private static TokenProvider requireToken(TokenProvider tokenProvider, AuthType type) {
        if (tokenProvider == null) {
            throw new ConfigurationException("A TokenProvider is required for auth.type=" + type);
        }
        return tokenProvider;
    }

    private static String require(String value, String key) {
        if (value == null || value.isBlank()) {
            throw new ConfigurationException(key + " is required for the configured auth.type");
        }
        return value;
    }
}
