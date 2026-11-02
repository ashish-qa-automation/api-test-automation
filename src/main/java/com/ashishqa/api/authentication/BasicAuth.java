package com.ashishqa.api.authentication;

import io.restassured.specification.RequestSpecification;

import java.util.Objects;

public final class BasicAuth implements AuthStrategy {

    private final String username;
    private final String password;

    public BasicAuth(String username, String password) {
        this.username = Objects.requireNonNull(username, "username");
        this.password = Objects.requireNonNull(password, "password");
    }

    @Override
    public void apply(RequestSpecification spec) {
        spec.auth().preemptive().basic(username, password);
    }
}
