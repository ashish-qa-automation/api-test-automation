package com.ashishqa.api.config;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Process-wide holder for the immutable {@link FrameworkConfig} snapshot.
 * Tests may {@link #reset()} then {@link #initialize(ConfigLoader)} with a fake override source.
 */
public final class ConfigManager {

    private static final AtomicReference<FrameworkConfig> ACTIVE = new AtomicReference<>();

    private ConfigManager() {
    }

    public static FrameworkConfig initialize() {
        return initialize(new ConfigLoader());
    }

    public static FrameworkConfig initialize(ConfigLoader loader) {
        FrameworkConfig loaded = loader.load();
        ACTIVE.set(loaded);
        return loaded;
    }

    public static FrameworkConfig current() {
        FrameworkConfig config = ACTIVE.get();
        if (config == null) {
            throw new ConfigurationException(
                    "Configuration has not been initialized. Call ConfigManager.initialize() first.");
        }
        return config;
    }

    public static boolean isInitialized() {
        return ACTIVE.get() != null;
    }

    public static void reset() {
        ACTIVE.set(null);
    }
}
