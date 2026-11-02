package com.ashishqa.api.authentication;

import java.util.Objects;

/**
 * Caches one token per thread so parallel tests do not overwrite each other.
 */
public final class ThreadLocalTokenProvider implements TokenProvider {

    private final ThreadLocal<String> cache = new ThreadLocal<>();
    private final TokenSource source;

    public ThreadLocalTokenProvider(TokenSource source) {
        this.source = Objects.requireNonNull(source, "source");
    }

    @Override
    public String token() {
        String current = cache.get();
        if (current == null || current.isBlank()) {
            String fetched = source.fetch();
            if (fetched == null || fetched.isBlank()) {
                throw new AuthenticationException("Token source returned a blank token");
            }
            cache.set(fetched);
            return fetched;
        }
        return current;
    }

    @Override
    public void invalidate() {
        cache.remove();
    }
}
