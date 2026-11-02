package com.ashishqa.api.authentication;

/**
 * Supplies an access token. Implementations that fetch tokens must be thread-safe
 * for parallel TestNG execution.
 */
public interface TokenProvider {

    String token();

    default void invalidate() {
        // static tokens have nothing to clear
    }
}
