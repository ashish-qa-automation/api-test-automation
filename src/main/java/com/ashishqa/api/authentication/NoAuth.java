package com.ashishqa.api.authentication;

import io.restassured.specification.RequestSpecification;

public final class NoAuth implements AuthStrategy {

    public static final NoAuth INSTANCE = new NoAuth();

    private NoAuth() {
    }

    @Override
    public void apply(RequestSpecification spec) {
        // public endpoints
    }
}
