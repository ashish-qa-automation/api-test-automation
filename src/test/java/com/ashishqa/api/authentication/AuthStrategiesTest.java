package com.ashishqa.api.authentication;

import com.ashishqa.api.config.ConfigLoader;
import com.ashishqa.api.config.FrameworkConfig;
import com.ashishqa.api.config.MapConfigOverrideSource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class AuthStrategiesTest {

    @Test(groups = "smoke")
    public void qaConfigUsesCookieTokenStrategy() {
        FrameworkConfig config = new ConfigLoader(new MapConfigOverrideSource(Map.of(), Map.of())).load();
        AuthStrategy strategy = AuthStrategies.from(config, () -> "static-token");
        Assert.assertEquals(strategy.getClass(), CookieTokenAuth.class);
        Assert.assertEquals(config.authTokenEndpoint(), "/auth");
    }

    @Test(groups = "smoke")
    public void devConfigUsesNoAuth() {
        FrameworkConfig config = new ConfigLoader(new MapConfigOverrideSource(Map.of("env", "dev"), Map.of())).load();
        AuthStrategy strategy = AuthStrategies.from(config, () -> "unused");
        Assert.assertSame(strategy, NoAuth.INSTANCE);
    }
}
