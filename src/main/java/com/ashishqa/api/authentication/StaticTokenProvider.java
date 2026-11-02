package com.ashishqa.api.authentication;

import java.util.Objects;

public final class StaticTokenProvider implements TokenProvider {

    private final String token;

    public StaticTokenProvider(String token) {
        this.token = Objects.requireNonNull(token, "token");
    }

    @Override
    public String token() {
        return token;
    }
}
