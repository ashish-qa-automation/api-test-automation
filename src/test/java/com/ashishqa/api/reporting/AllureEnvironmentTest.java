package com.ashishqa.api.reporting;

import com.ashishqa.api.config.ConfigLoader;
import com.ashishqa.api.config.FrameworkConfig;
import com.ashishqa.api.config.MapConfigOverrideSource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class AllureEnvironmentTest {

    @Test(groups = "smoke")
    public void environmentFileRecordsRuntimeAndOmitsSecrets() throws Exception {
        FrameworkConfig config = new ConfigLoader(new MapConfigOverrideSource(Map.of("env", "qa"), Map.of())).load();
        Path directory = Files.createTempDirectory("allure-env");
        AllureEnvironment.write(directory, config, "Smoke");

        String text = Files.readString(directory.resolve("environment.properties"));
        Assert.assertTrue(text.contains("Environment=qa"));
        Assert.assertTrue(text.contains("BaseUri=https://restful-booker.herokuapp.com"));
        Assert.assertTrue(text.contains("Suite=Smoke"));
        Assert.assertTrue(text.contains("Java="));
        Assert.assertTrue(text.contains("OS="));
        Assert.assertFalse(text.contains("password123"));
        Assert.assertFalse(text.contains("auth.password"));
    }
}
