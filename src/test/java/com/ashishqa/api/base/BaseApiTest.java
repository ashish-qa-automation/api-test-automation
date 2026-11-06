package com.ashishqa.api.base;

import com.ashishqa.api.authentication.AuthStrategy;
import com.ashishqa.api.authentication.AuthStrategies;
import com.ashishqa.api.authentication.NoAuth;
import com.ashishqa.api.authentication.TokenProvider;
import com.ashishqa.api.authentication.TokenProviders;
import com.ashishqa.api.clients.RestClient;
import com.ashishqa.api.config.ConfigManager;
import com.ashishqa.api.config.FrameworkConfig;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

/**
 * Loads process configuration once per test class. HTTP clients are created per use
 * so parallel tests do not share a mutable request specification.
 */
public abstract class BaseApiTest {

    private volatile TokenProvider tokenProvider;
    private volatile AuthStrategy authStrategy;

    @BeforeClass(alwaysRun = true)
    protected void initializeFramework() {
        ConfigManager.initialize();
        RestClient anonymous = new RestClient(config(), NoAuth.INSTANCE);
        tokenProvider = TokenProviders.from(config(), anonymous);
        authStrategy = AuthStrategies.from(config(), tokenProvider);
    }

    @AfterMethod(alwaysRun = true)
    protected void clearThreadToken() {
        if (tokenProvider != null) {
            tokenProvider.invalidate();
        }
    }

    protected FrameworkConfig config() {
        return ConfigManager.current();
    }

    protected RestClient restClient() {
        return new RestClient(config(), authStrategy);
    }

    protected RestClient anonymousRestClient() {
        return new RestClient(config(), NoAuth.INSTANCE);
    }

    protected TokenProvider tokenProvider() {
        return tokenProvider;
    }
}
