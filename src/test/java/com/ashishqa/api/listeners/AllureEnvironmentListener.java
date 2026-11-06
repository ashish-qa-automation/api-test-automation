package com.ashishqa.api.listeners;

import com.ashishqa.api.config.ConfigManager;
import com.ashishqa.api.reporting.AllureEnvironment;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import java.nio.file.Path;

/**
 * Writes the Allure environment file once per suite, before tests send traffic.
 */
public final class AllureEnvironmentListener implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        if (!ConfigManager.isInitialized()) {
            ConfigManager.initialize();
        }
        String configured = System.getProperty("allure.results.directory", "target/allure-results");
        AllureEnvironment.write(Path.of(configured), ConfigManager.current(), suite.getName());
    }
}
