package com.ashishqa.api.config;

import com.ashishqa.api.authentication.AuthType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * Loads {@link FrameworkConfig} using:
 * system properties → environment variables → {@code config/env/{env}.properties} → {@code config/framework.properties}.
 */
public final class ConfigLoader {

    public static final String DEFAULT_ENVIRONMENT = "qa";
    public static final Set<String> SUPPORTED_ENVIRONMENTS = Set.of("dev", "qa", "sit", "uat");

    static final String DEFAULTS_RESOURCE = "config/framework.properties";
    static final String ENV_RESOURCE_TEMPLATE = "config/env/%s.properties";

    private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);

    private final ConfigOverrideSource overrides;
    private final ClassLoader classLoader;

    public ConfigLoader() {
        this(new SystemConfigOverrideSource(), defaultClassLoader());
    }

    public ConfigLoader(ConfigOverrideSource overrides) {
        this(overrides, defaultClassLoader());
    }

    ConfigLoader(ConfigOverrideSource overrides, ClassLoader classLoader) {
        this.overrides = Objects.requireNonNull(overrides, "overrides");
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
    }

    public FrameworkConfig load() {
        Properties defaults = loadProperties(DEFAULTS_RESOURCE, true);
        String environment = resolveEnvironment(defaults);
        Properties envFile = loadProperties(String.format(ENV_RESOURCE_TEMPLATE, environment), true);

        FrameworkConfig config = new FrameworkConfig(
                environment,
                required(ConfigKeys.API_BASE_URI, envFile, defaults),
                optional(ConfigKeys.API_BASE_PATH, envFile, defaults),
                integer(ConfigKeys.HTTP_CONNECT_TIMEOUT_MS, envFile, defaults),
                integer(ConfigKeys.HTTP_RESPONSE_TIMEOUT_MS, envFile, defaults),
                bool(ConfigKeys.HTTP_LOG_REQUESTS, envFile, defaults),
                bool(ConfigKeys.HTTP_LOG_RESPONSES, envFile, defaults),
                AuthType.from(optional(ConfigKeys.AUTH_TYPE, envFile, defaults)),
                optional(ConfigKeys.AUTH_USERNAME, envFile, defaults),
                optional(ConfigKeys.AUTH_PASSWORD, envFile, defaults),
                optional(ConfigKeys.AUTH_TOKEN, envFile, defaults),
                optional(ConfigKeys.AUTH_API_KEY_HEADER, envFile, defaults),
                optional(ConfigKeys.AUTH_API_KEY, envFile, defaults),
                optional(ConfigKeys.AUTH_TOKEN_ENDPOINT, envFile, defaults),
                optional(ConfigKeys.AUTH_TOKEN_FIELD, envFile, defaults)
        );
        log.info("Loaded configuration for env={} baseUri={}", config.environment(), config.baseUri());
        return config;
    }

    String resolveEnvironment(Properties defaults) {
        String raw = firstPresent(
                overrides.getProperty(ConfigKeys.ENV),
                overrides.getEnv("ENV"),
                optionalProperty(defaults, ConfigKeys.ENV)
        ).orElse(DEFAULT_ENVIRONMENT);
        String environment = raw.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_ENVIRONMENTS.contains(environment)) {
            throw new ConfigurationException(
                    "Unknown env '" + raw + "'. Supported: " + SUPPORTED_ENVIRONMENTS);
        }
        return environment;
    }

    static String toEnvName(String key) {
        StringBuilder out = new StringBuilder(key.length() + 4);
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (c == '.') {
                out.append('_');
            } else if (Character.isUpperCase(c)) {
                if (i > 0 && key.charAt(i - 1) != '.') {
                    out.append('_');
                }
                out.append(c);
            } else {
                out.append(Character.toUpperCase(c));
            }
        }
        return out.toString();
    }

    private String required(String key, Properties envFile, Properties defaults) {
        String value = resolve(key, envFile, defaults);
        if (value.isBlank()) {
            throw new ConfigurationException(key + " is required for the active environment");
        }
        return value;
    }

    private String optional(String key, Properties envFile, Properties defaults) {
        return resolve(key, envFile, defaults);
    }

    private int integer(String key, Properties envFile, Properties defaults) {
        String value = required(key, envFile, defaults);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new ConfigurationException(key + " must be an integer, found '" + value + "'", ex);
        }
    }

    private boolean bool(String key, Properties envFile, Properties defaults) {
        String value = required(key, envFile, defaults).toLowerCase(Locale.ROOT);
        if ("true".equals(value) || "false".equals(value)) {
            return Boolean.parseBoolean(value);
        }
        throw new ConfigurationException(key + " must be true or false, found '" + value + "'");
    }

    private String resolve(String key, Properties envFile, Properties defaults) {
        Optional<String> systemProperty = overrides.getProperty(key);
        if (systemProperty.isPresent()) {
            return systemProperty.get().trim();
        }
        Optional<String> environmentVariable = overrides.getEnv(toEnvName(key));
        if (environmentVariable.isPresent()) {
            return environmentVariable.get().trim();
        }
        String fromEnvFile = envFile.getProperty(key);
        if (fromEnvFile != null && !fromEnvFile.isBlank()) {
            return fromEnvFile.trim();
        }
        String fromDefaults = defaults.getProperty(key);
        if (fromDefaults != null && !fromDefaults.isBlank()) {
            return fromDefaults.trim();
        }
        return "";
    }

    private Properties loadProperties(String resource, boolean required) {
        try (InputStream in = classLoader.getResourceAsStream(resource)) {
            if (in == null) {
                if (required) {
                    throw new ConfigurationException("Missing classpath resource: " + resource);
                }
                return new Properties();
            }
            Properties properties = new Properties();
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
            return properties;
        } catch (IOException ex) {
            throw new ConfigurationException("Unable to read classpath resource: " + resource, ex);
        }
    }

    @SafeVarargs
    private static Optional<String> firstPresent(Optional<String>... values) {
        for (Optional<String> value : values) {
            if (value.isPresent() && !value.get().isBlank()) {
                return value;
            }
        }
        return Optional.empty();
    }

    private static Optional<String> optionalProperty(Properties properties, String key) {
        String value = properties.getProperty(key);
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
    }

    private static ClassLoader defaultClassLoader() {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        return context != null ? context : ConfigLoader.class.getClassLoader();
    }
}
