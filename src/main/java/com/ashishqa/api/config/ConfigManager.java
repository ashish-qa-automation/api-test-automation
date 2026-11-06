package com.ashishqa.api.config;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Process-wide holder for the immutable {@link FrameworkConfig} snapshot.
 * Readers and writers share one lock so a test can swap the snapshot without other threads observing it.
 */
public final class ConfigManager {

    private static final Object LOCK = new Object();
    private static final AtomicReference<FrameworkConfig> ACTIVE = new AtomicReference<>();

    private ConfigManager() {
    }

    public static FrameworkConfig initialize() {
        return initialize(new ConfigLoader());
    }

    public static FrameworkConfig initialize(ConfigLoader loader) {
        synchronized (LOCK) {
            FrameworkConfig loaded = loader.load();
            ACTIVE.set(loaded);
            return loaded;
        }
    }

    public static FrameworkConfig current() {
        synchronized (LOCK) {
            FrameworkConfig config = ACTIVE.get();
            if (config == null) {
                throw new ConfigurationException(
                        "Configuration has not been initialized. Call ConfigManager.initialize() first.");
            }
            return config;
        }
    }

    public static boolean isInitialized() {
        synchronized (LOCK) {
            return ACTIVE.get() != null;
        }
    }

    public static void reset() {
        synchronized (LOCK) {
            ACTIVE.set(null);
        }
    }

    /**
     * Runs {@code action} without letting other threads observe a cleared or replaced snapshot.
     * The previous snapshot is restored when {@code action} returns.
     */
    public static void exclusively(Runnable action) {
        synchronized (LOCK) {
            FrameworkConfig previous = ACTIVE.get();
            try {
                ACTIVE.set(null);
                action.run();
            } finally {
                ACTIVE.set(previous);
            }
        }
    }
}
