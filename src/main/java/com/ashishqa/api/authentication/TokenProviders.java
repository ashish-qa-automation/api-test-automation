package com.ashishqa.api.authentication;

import com.ashishqa.api.clients.RestClient;
import com.ashishqa.api.config.ConfigurationException;
import com.ashishqa.api.config.FrameworkConfig;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class TokenProviders {

    private TokenProviders() {
    }

    /**
     * Uses {@code auth.token} when set; otherwise fetches from {@code auth.tokenEndpoint}.
     */
    public static TokenProvider from(FrameworkConfig config, RestClient anonymousClient) {
        Objects.requireNonNull(config, "config");
        if (!config.authToken().isBlank()) {
            return new StaticTokenProvider(config.authToken());
        }
        if (config.authType() != AuthType.BEARER && config.authType() != AuthType.COOKIE_TOKEN) {
            return new StaticTokenProvider("");
        }
        if (config.authTokenEndpoint().isBlank()) {
            throw new ConfigurationException(
                    "auth.tokenEndpoint is required when auth.type=" + config.authType());
        }
        Map<String, String> body = new LinkedHashMap<>();
        body.put("username", config.authUsername());
        body.put("password", config.authPassword());
        return new ThreadLocalTokenProvider(
                new JsonTokenSource(anonymousClient, config.authTokenEndpoint(), config.authTokenField(), body));
    }
}
