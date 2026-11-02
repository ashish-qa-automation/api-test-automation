package com.ashishqa.api.logging;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SensitiveDataRedactorTest {

    private final SensitiveDataRedactor redactor = new SensitiveDataRedactor();

    @Test(groups = "smoke")
    public void redactsJsonPasswordAndToken() {
        String redacted = redactor.redact(
                "{\"username\":\"admin\",\"password\":\"password123\",\"token\":\"abc-secret\"}");
        Assert.assertFalse(redacted.contains("password123"));
        Assert.assertFalse(redacted.contains("abc-secret"));
        Assert.assertTrue(redacted.contains("\"password\":\"***\""));
        Assert.assertTrue(redacted.contains("\"token\":\"***\""));
    }

    @Test(groups = "smoke")
    public void redactsAuthorizationAndCookieHeaders() {
        String redacted = redactor.redact(
                "Authorization: Bearer abc-secret\nCookie: token=abc-secret");
        Assert.assertFalse(redacted.contains("abc-secret"));
        Assert.assertTrue(redacted.contains("Authorization: ***"));
        Assert.assertTrue(redacted.contains("Cookie: ***"));
    }
}
