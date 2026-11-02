package com.ashishqa.api.config;

import com.ashishqa.api.authentication.AuthType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class ConfigLoaderTest {

    @Test(groups = "smoke")
    public void defaultEnvironmentIsQaAndUsesRestfulBooker() {
        FrameworkConfig config = load(Map.of(), Map.of());
        Assert.assertEquals(config.environment(), "qa");
        Assert.assertEquals(config.baseUri(), "https://restful-booker.herokuapp.com");
        Assert.assertEquals(config.authType(), AuthType.COOKIE_TOKEN);
        Assert.assertEquals(config.authTokenEndpoint(), "/auth");
        Assert.assertEquals(config.connectTimeoutMs(), 15000);
        Assert.assertEquals(config.responseTimeoutMs(), 45000);
    }

    @Test(groups = "smoke")
    public void envSystemPropertySwitchesToDevWithoutCodeChanges() {
        FrameworkConfig config = load(Map.of("env", "dev"), Map.of());
        Assert.assertEquals(config.environment(), "dev");
        Assert.assertEquals(config.baseUri(), "http://localhost:8080");
        Assert.assertEquals(config.authType(), AuthType.NONE);
        Assert.assertNotEquals(config.baseUri(), "https://restful-booker.herokuapp.com");
    }

    @Test(groups = "smoke")
    public void envVariableSelectsSit() {
        FrameworkConfig config = load(Map.of(), Map.of("ENV", "sit"));
        Assert.assertEquals(config.environment(), "sit");
        Assert.assertEquals(config.baseUri(), "https://sit.example.invalid");
    }

    @Test(groups = "smoke")
    public void envNameIsCaseInsensitive() {
        FrameworkConfig config = load(Map.of("env", "UAT"), Map.of());
        Assert.assertEquals(config.environment(), "uat");
        Assert.assertEquals(config.baseUri(), "https://uat.example.invalid");
    }

    @Test(groups = "smoke")
    public void systemPropertyOverridesEnvironmentFile() {
        FrameworkConfig config = load(
                Map.of("env", "qa", "api.baseUri", "https://from-system.example"),
                Map.of());
        Assert.assertEquals(config.baseUri(), "https://from-system.example");
    }

    @Test(groups = "smoke")
    public void environmentVariableOverridesFileButLosesToSystemProperty() {
        FrameworkConfig fromEnv = load(
                Map.of("env", "qa"),
                Map.of("API_BASE_URI", "https://from-env.example"));
        Assert.assertEquals(fromEnv.baseUri(), "https://from-env.example");

        FrameworkConfig fromSystem = load(
                Map.of("env", "qa", "api.baseUri", "https://from-system.example"),
                Map.of("API_BASE_URI", "https://from-env.example"));
        Assert.assertEquals(fromSystem.baseUri(), "https://from-system.example");
    }

    @Test(groups = "smoke")
    public void blankSystemPropertyClearsFilePassword() {
        FrameworkConfig config = load(
                Map.of("env", "qa", "auth.password", ""),
                Map.of());
        Assert.assertEquals(config.authPassword(), "");
    }

    @Test(groups = "smoke")
    public void unknownEnvironmentFailsClearly() {
        try {
            load(Map.of("env", "prod"), Map.of());
            Assert.fail("Expected ConfigurationException");
        } catch (ConfigurationException ex) {
            Assert.assertTrue(ex.getMessage().contains("prod"));
            Assert.assertTrue(ex.getMessage().contains("qa"));
        }
    }

    @Test(groups = "smoke")
    public void missingBaseUriFailsClearly() {
        try {
            load(Map.of("env", "dev", "api.baseUri", ""), Map.of());
            Assert.fail("Expected ConfigurationException");
        } catch (ConfigurationException ex) {
            Assert.assertTrue(ex.getMessage().contains("api.baseUri"));
        }
    }

    @Test(groups = "smoke")
    public void toStringRedactsSecrets() {
        FrameworkConfig config = load(Map.of(), Map.of());
        String text = config.toString();
        Assert.assertFalse(text.contains("password123"));
        Assert.assertTrue(text.contains("authPassword='***'"));
        Assert.assertTrue(text.contains("environment='qa'"));
    }

    @Test(groups = "smoke")
    public void envVarNameUsesUpperSnakeCase() {
        Assert.assertEquals(ConfigLoader.toEnvName("api.baseUri"), "API_BASE_URI");
        Assert.assertEquals(ConfigLoader.toEnvName("http.connectTimeoutMs"), "HTTP_CONNECT_TIMEOUT_MS");
        Assert.assertEquals(ConfigLoader.toEnvName("auth.apiKeyHeader"), "AUTH_API_KEY_HEADER");
        Assert.assertEquals(ConfigLoader.toEnvName("auth.tokenEndpoint"), "AUTH_TOKEN_ENDPOINT");
    }

    @Test(groups = "smoke")
    public void configManagerHoldsImmutableSnapshot() {
        ConfigManager.reset();
        try {
            FrameworkConfig loaded = ConfigManager.initialize(
                    new ConfigLoader(new MapConfigOverrideSource(Map.of("env", "dev"), Map.of())));
            Assert.assertSame(loaded, ConfigManager.current());
            Assert.assertEquals(ConfigManager.current().environment(), "dev");
        } finally {
            ConfigManager.reset();
        }
        Assert.assertFalse(ConfigManager.isInitialized());
    }

    private static FrameworkConfig load(Map<String, String> properties, Map<String, String> env) {
        return new ConfigLoader(new MapConfigOverrideSource(properties, env)).load();
    }
}
