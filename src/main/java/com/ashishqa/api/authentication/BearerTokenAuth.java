package com.ashishqa.api.authentication;

import io.restassured.specification.RequestSpecification;

import java.util.Objects;

public final class BearerTokenAuth implements AuthStrategy {

    private final TokenProvider tokenProvider;

    public BearerTokenAuth(TokenProvider tokenProvider) {
        this.tokenProvider = Objects.requireNonNull(tokenProvider, "tokenProvider");
    }

    @Override
    public void apply(RequestSpecification spec) {
        spec.header("Authorization", "Bearer " + tokenProvider.token());
    }
}
