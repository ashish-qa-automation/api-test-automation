package com.ashishqa.api.authentication;

import io.restassured.specification.RequestSpecification;

import java.util.Objects;

/**
 * Restful Booker write operations expect {@code Cookie: token=&lt;value&gt;}, not a Bearer header.
 */
public final class CookieTokenAuth implements AuthStrategy {

    private final TokenProvider tokenProvider;

    public CookieTokenAuth(TokenProvider tokenProvider) {
        this.tokenProvider = Objects.requireNonNull(tokenProvider, "tokenProvider");
    }

    @Override
    public void apply(RequestSpecification spec) {
        spec.cookie("token", tokenProvider.token());
    }
}
