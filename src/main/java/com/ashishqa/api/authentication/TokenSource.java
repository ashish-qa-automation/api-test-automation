package com.ashishqa.api.authentication;

/**
 * Fetches a new token from an identity endpoint. {@link ThreadLocalTokenProvider} caches the result.
 */
public interface TokenSource {

    String fetch();
}
