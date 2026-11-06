package com.ashishqa.api.reporting;

import com.ashishqa.api.authentication.AuthType;
import com.ashishqa.api.clients.ApiRequest;
import com.ashishqa.api.clients.RestClient;
import com.ashishqa.api.config.FrameworkConfig;
import com.ashishqa.api.logging.CorrelationId;
import com.sun.net.httpserver.HttpServer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A local HTTP call proves Allure attachments keep the exchange and drop the secret.
 */
public class AllureRedactionTest {

    private static final String SECRET = "phase9-secret-token-value";

    @Test(groups = "smoke")
    public void allureAttachmentsRedactTheToken() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/secret", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] body = ("{\"token\":\"" + SECRET + "\"}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            Path results = Path.of("target", "allure-results");
            Files.createDirectories(results);
            Set<Path> before = snapshot(results);
            int port = server.getAddress().getPort();
            FrameworkConfig config = new FrameworkConfig(
                    "qa",
                    "http://localhost:" + port,
                    "",
                    5000,
                    5000,
                    true,
                    true,
                    AuthType.NONE,
                    "",
                    "",
                    "",
                    "X-Api-Key",
                    "",
                    "",
                    "token");
            CorrelationId.start();
            try {
                new RestClient(config).post(ApiRequest.to("/secret").anonymous().body(
                        java.util.Map.of("password", "password123", "token", SECRET)));
            } finally {
                CorrelationId.clear();
            }

            Set<Path> created = snapshot(results);
            created.removeAll(before);
            Assert.assertFalse(created.isEmpty(), "Allure did not write an attachment");
            String combined = read(created);
            Assert.assertFalse(combined.contains(SECRET), combined);
            Assert.assertFalse(combined.contains("password123"), combined);
            Assert.assertTrue(combined.contains("***"), combined);
            Assert.assertTrue(combined.contains("X-Correlation-Id") || combined.contains("correlation"), combined);
        } finally {
            server.stop(0);
        }
    }

    private static Set<Path> snapshot(Path directory) throws Exception {
        Set<Path> paths = new HashSet<>();
        try (Stream<Path> files = Files.list(directory)) {
            files.forEach(paths::add);
        }
        return paths;
    }

    private static String read(Set<Path> files) throws Exception {
        StringBuilder text = new StringBuilder();
        for (Path file : files) {
            if (Files.isRegularFile(file)) {
                text.append(Files.readString(file)).append('\n');
            }
        }
        return text.toString();
    }
}
