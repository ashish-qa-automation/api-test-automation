package com.ashishqa.api.config;

/**
 * Thrown when configuration files, overrides, or values cannot be used as-is.
 */
public class ConfigurationException extends RuntimeException {

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
