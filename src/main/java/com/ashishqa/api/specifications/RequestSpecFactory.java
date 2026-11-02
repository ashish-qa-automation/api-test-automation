package com.ashishqa.api.specifications;

import com.ashishqa.api.config.FrameworkConfig;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.Objects;
import java.util.Set;

/**
 * Builds a fresh REST Assured request specification from {@link FrameworkConfig}.
 * Callers must not mutate a returned spec and reuse it across threads.
 */
public final class RequestSpecFactory {

    private static final Set<String> REDACTED_HEADERS = Set.of("Authorization", "Cookie", "X-Api-Key");

    private final FrameworkConfig config;

    public RequestSpecFactory(FrameworkConfig config) {
        this.config = Objects.requireNonNull(config, "config");
    }

    public RequestSpecification create() {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(config.baseUri())
                .setAccept(ContentType.JSON)
                .setConfig(restAssuredConfig());

        if (!config.basePath().isBlank()) {
            builder.setBasePath(config.basePath());
        }

        if (config.logRequests()) {
            builder.addFilter(new RequestLoggingFilter(LogDetail.ALL, true, System.out, true, REDACTED_HEADERS));
        }
        if (config.logResponses()) {
            builder.addFilter(new ResponseLoggingFilter(LogDetail.ALL, true, System.out));
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
