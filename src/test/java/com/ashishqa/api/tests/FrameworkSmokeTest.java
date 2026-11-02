package com.ashishqa.api.tests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Foundation checks that do not call an external API.
 * Proves the Maven, TestNG, logging and core library classpath work together.
 */
public class FrameworkSmokeTest {

    private static final Logger log = LoggerFactory.getLogger(FrameworkSmokeTest.class);

    @Test(groups = "smoke")
    public void javaRuntimeMeetsBaseline() {
        int feature = Runtime.version().feature();
        log.info("Java runtime feature version: {}", feature);
        Assert.assertTrue(feature >= 17, "Java 17 or newer is required, found " + feature);
    }

    @Test(groups = "smoke")
    public void restAssuredIsOnClasspath() throws ClassNotFoundException {
        Class.forName("io.restassured.RestAssured");
        log.info("REST Assured is available on the test classpath");
    }

    @Test(groups = "smoke")
    public void jacksonIsOnClasspath() throws ClassNotFoundException {
        Class.forName("com.fasterxml.jackson.databind.ObjectMapper");
        log.info("Jackson databind is available on the classpath");
    }

    @Test(groups = "smoke")
    public void jsonSchemaValidatorIsOnClasspath() throws ClassNotFoundException {
        Class.forName("io.restassured.module.jsv.JsonSchemaValidator");
        log.info("REST Assured JSON Schema validator is available on the test classpath");
    }

    @Test(groups = "smoke")
    public void slf4jLoggerIsBound() {
        Assert.assertFalse(log.getClass().getName().contains("NOPLogger"),
                "SLF4J should be bound to Log4j2, not a no-op logger");
        log.info("SLF4J is bound to {}", log.getClass().getName());
    }
}
