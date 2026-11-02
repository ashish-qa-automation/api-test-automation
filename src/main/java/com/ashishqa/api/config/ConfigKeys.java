package com.ashishqa.api.config;

/**
 * Property keys used in configuration files, JVM system properties, and environment variables.
 * Environment variables are the upper-snake-case form, for example {@code api.baseUri} → {@code API_BASE_URI}.
 */
public final class ConfigKeys {

    public static final String ENV = "env";

    public static final String API_BASE_URI = "api.baseUri";
    public static final String API_BASE_PATH = "api.basePath";

    public static final String HTTP_CONNECT_TIMEOUT_MS = "http.connectTimeoutMs";
    public static final String HTTP_RESPONSE_TIMEOUT_MS = "http.responseTimeoutMs";
    public static final String HTTP_LOG_REQUESTS = "http.logRequests";
    public static final String HTTP_LOG_RESPONSES = "http.logResponses";

    public static final String AUTH_TYPE = "auth.type";
    public static final String AUTH_USERNAME = "auth.username";
    public static final String AUTH_PASSWORD = "auth.password";
    public static final String AUTH_TOKEN = "auth.token";
    public static final String AUTH_API_KEY_HEADER = "auth.apiKeyHeader";
    public static final String AUTH_API_KEY = "auth.apiKey";
    public static final String AUTH_TOKEN_ENDPOINT = "auth.tokenEndpoint";
    public static final String AUTH_TOKEN_FIELD = "auth.tokenField";

    private ConfigKeys() {
    }
}
