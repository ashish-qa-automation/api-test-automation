package com.ashishqa.api.config;

import com.ashishqa.api.authentication.AuthType;

import java.util.Objects;

/**
 * Immutable snapshot of the active environment. Safe to share across threads.
 */
public record FrameworkConfig(
        String environment,
        String baseUri,
        String basePath,
        int connectTimeoutMs,
        int responseTimeoutMs,
        boolean logRequests,
        boolean logResponses,
        AuthType authType,
        String authUsername,
        String authPassword,
        String authToken,
        String apiKeyHeader,
        String apiKey
) {

    public FrameworkConfig {
        environment = requireText("environment", environment);
        baseUri = requireText("api.baseUri", baseUri);
        basePath = Objects.requireNonNullElse(basePath, "");
        authType = Objects.requireNonNull(authType, "authType");
        authUsername = Objects.requireNonNullElse(authUsername, "");
        authPassword = Objects.requireNonNullElse(authPassword, "");
        authToken = Objects.requireNonNullElse(authToken, "");
        apiKeyHeader = Objects.requireNonNullElse(apiKeyHeader, "");
        apiKey = Objects.requireNonNullElse(apiKey, "");
        if (connectTimeoutMs <= 0) {
            throw new ConfigurationException("http.connectTimeoutMs must be a positive integer");
        }
        if (responseTimeoutMs <= 0) {
            throw new ConfigurationException("http.responseTimeoutMs must be a positive integer");
        }
    }

    public String baseUrl() {
        if (basePath.isBlank()) {
            return baseUri;
        }
        if (baseUri.endsWith("/") && basePath.startsWith("/")) {
            return baseUri + basePath.substring(1);
        }
        if (!baseUri.endsWith("/") && !basePath.startsWith("/")) {
            return baseUri + "/" + basePath;
        }
        return baseUri + basePath;
    }

    @Override
    public String toString() {
        return "FrameworkConfig{"
                + "environment='" + environment + '\''
                + ", baseUri='" + baseUri + '\''
                + ", basePath='" + basePath + '\''
                + ", connectTimeoutMs=" + connectTimeoutMs
                + ", responseTimeoutMs=" + responseTimeoutMs
                + ", logRequests=" + logRequests
                + ", logResponses=" + logResponses
                + ", authType=" + authType
                + ", authUsername='" + authUsername + '\''
                + ", authPassword='" + redact(authPassword) + '\''
                + ", authToken='" + redact(authToken) + '\''
                + ", apiKeyHeader='" + apiKeyHeader + '\''
                + ", apiKey='" + redact(apiKey) + '\''
                + '}';
    }

    private static String requireText(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new ConfigurationException(name + " is required");
        }
        return value.trim();
    }

    private static String redact(String value) {
        return value == null || value.isBlank() ? "" : "***";
    }
}
