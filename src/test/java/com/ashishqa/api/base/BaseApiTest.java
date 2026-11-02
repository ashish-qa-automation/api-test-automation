package com.ashishqa.api.base;

import com.ashishqa.api.clients.RestClient;
import com.ashishqa.api.config.ConfigManager;
import com.ashishqa.api.config.FrameworkConfig;
import org.testng.annotations.BeforeClass;

/**
 * Loads process configuration once per test class. HTTP clients are created per use
 * so parallel tests do not share a mutable request specification.
 */
public abstract class BaseApiTest {

    @BeforeClass(alwaysRun = true)
    protected void initializeFramework() {
        ConfigManager.initialize();
    }

    protected FrameworkConfig config() {
        return ConfigManager.current();
    }

    protected RestClient restClient() {
        return new RestClient(config());
    }
}
