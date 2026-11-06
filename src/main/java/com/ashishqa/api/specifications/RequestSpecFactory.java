package com.ashishqa.api.specifications;

import com.ashishqa.api.config.FrameworkConfig;
import com.ashishqa.api.logging.CorrelationId;
import com.ashishqa.api.logging.RedactingLoggingFilter;
import com.ashishqa.api.logging.SensitiveDataRedactor;
import com.ashishqa.api.reporting.RedactingAllureFilter;
import com.ashishqa.api.serialization.JsonCodec;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.mapper.ObjectMapperType;
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
                .addFilter(new RedactingAllureFilter(redactor))
                .setConfig(restAssuredConfig());

        String correlationId = CorrelationId.current();
        if (!correlationId.isBlank()) {
            builder.addHeader("X-Correlation-Id", correlationId);
        }

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
        return RestAssuredConfig.newConfig()
                .httpClient(httpClientConfig)
                .objectMapperConfig(ObjectMapperConfig.objectMapperConfig()
                        .defaultObjectMapperType(ObjectMapperType.JACKSON_2)
                        .jackson2ObjectMapperFactory((type, charset) -> JsonCodec.instance().mapper()));
    }
}
