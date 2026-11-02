package com.ashishqa.api.specifications;

import com.ashishqa.api.config.FrameworkConfig;
import com.ashishqa.api.logging.RedactingLoggingFilter;
import com.ashishqa.api.logging.SensitiveDataRedactor;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;

import java.util.Objects;

/**
 * Builds a fresh REST Assured request specification from {@link FrameworkConfig}.
 * Callers must not mutate a returned spec and reuse it across threads.
 */
public final class RequestSpecFactory {

    private final FrameworkConfig config;
    private final SensitiveDataRedactor redactor;

    public RequestSpecFactory(FrameworkConfig config) {
        this(config, new SensitiveDataRedactor());
    }

    public RequestSpecFactory(FrameworkConfig config, SensitiveDataRedactor redactor) {
        this.config = Objects.requireNonNull(config, "config");
        this.redactor = Objects.requireNonNull(redactor, "redactor");
    }

    public RequestSpecification create() {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(config.baseUri())
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "api-test-automation")
                .setConfig(restAssuredConfig());

        if (!config.basePath().isBlank()) {
            builder.setBasePath(config.basePath());
        }

        if (config.logRequests() || config.logResponses()) {
            builder.addFilter(new RedactingLoggingFilter(redactor, config.logRequests(), config.logResponses()));
        }

        return builder.build();
    }

    private RestAssuredConfig restAssuredConfig() {
        HttpClientConfig httpClientConfig = HttpClientConfig.httpClientConfig()
                .dontReuseHttpClientInstance()
                .setParam("http.connection.timeout", config.connectTimeoutMs())
                .setParam("http.socket.timeout", config.responseTimeoutMs());
        return RestAssuredConfig.newConfig().httpClient(httpClientConfig);
    }
}
