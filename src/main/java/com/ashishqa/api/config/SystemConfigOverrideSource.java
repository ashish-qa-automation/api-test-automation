package com.ashishqa.api.config;

import java.util.Optional;

/**
 * Reads JVM system properties and process environment variables.
 */
public final class SystemConfigOverrideSource implements ConfigOverrideSource {

    @Override
    public Optional<String> getProperty(String key) {
        return Optional.ofNullable(System.getProperty(key));
    }

    @Override
    public Optional<String> getEnv(String name) {
        return Optional.ofNullable(System.getenv(name));
    }
}
