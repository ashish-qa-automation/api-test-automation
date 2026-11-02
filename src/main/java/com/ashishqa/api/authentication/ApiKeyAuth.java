package com.ashishqa.api.authentication;

import io.restassured.specification.RequestSpecification;

import java.util.Objects;

public final class ApiKeyAuth implements AuthStrategy {

    private final String headerName;
    private final String apiKey;

    public ApiKeyAuth(String headerName, String apiKey) {
        this.headerName = headerName == null || headerName.isBlank() ? "X-Api-Key" : headerName;
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey");
    }

    @Override
    public void apply(RequestSpecification spec) {
        spec.header(headerName, apiKey);
    }
}
