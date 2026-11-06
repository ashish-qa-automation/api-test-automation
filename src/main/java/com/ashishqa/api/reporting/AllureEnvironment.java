package com.ashishqa.api.reporting;

import com.ashishqa.api.config.FrameworkConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Writes {@code environment.properties} into the Allure results directory.
 * Values are the active environment name and base URI, never credentials.
 */
public final class AllureEnvironment {

    private AllureEnvironment() {
    }

    public static void write(Path resultsDirectory, FrameworkConfig config, String suiteName) {
        Objects.requireNonNull(resultsDirectory, "resultsDirectory");
        Objects.requireNonNull(config, "config");
        String suite = suiteName == null || suiteName.isBlank() ? "unknown" : suiteName;
        String body = "Environment=" + config.environment() + System.lineSeparator()
                + "BaseUri=" + config.baseUri() + System.lineSeparator()
                + "Java=" + Runtime.version() + System.lineSeparator()
                + "OS=" + System.getProperty("os.name") + " " + System.getProperty("os.version") + System.lineSeparator()
                + "Suite=" + suite + System.lineSeparator();
        try {
            Files.createDirectories(resultsDirectory);
            Files.writeString(resultsDirectory.resolve("environment.properties"), body, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to write Allure environment.properties", ex);
        }
    }
}
