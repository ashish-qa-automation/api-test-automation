package com.ashishqa.api.config;

import java.util.Optional;

/**
 * Runtime overrides applied on top of classpath property files.
 * An empty optional means the key was not set. A present blank value is an intentional override.
 */
public interface ConfigOverrideSource {

    Optional<String> getProperty(String key);

    Optional<String> getEnv(String name);
}
