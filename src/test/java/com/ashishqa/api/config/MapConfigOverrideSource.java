package com.ashishqa.api.config;

import java.util.Map;
import java.util.Optional;

final class MapConfigOverrideSource implements ConfigOverrideSource {

    private final Map<String, String> properties;
    private final Map<String, String> env;

    MapConfigOverrideSource(Map<String, String> properties, Map<String, String> env) {
        this.properties = Map.copyOf(properties);
        this.env = Map.copyOf(env);
    }

    @Override
    public Optional<String> getProperty(String key) {
        return Optional.ofNullable(properties.get(key));
    }

    @Override
    public Optional<String> getEnv(String name) {
        return Optional.ofNullable(env.get(name));
    }
}
