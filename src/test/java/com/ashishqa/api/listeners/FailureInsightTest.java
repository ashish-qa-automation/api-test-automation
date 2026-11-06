package com.ashishqa.api.listeners;

import com.ashishqa.api.authentication.AuthenticationException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FailureInsightTest {

    @Test(groups = "smoke")
    public void assertionMessageKeepsExpectedAndActual() {
        String insight = FailureInsight.describe(
                new AssertionError("expected [200] but found [404]"),
                "corr-1");
        Assert.assertTrue(insight.contains("classification=assertion"));
        Assert.assertTrue(insight.contains("correlationId=corr-1"));
        Assert.assertTrue(insight.contains("expected=200"));
        Assert.assertTrue(insight.contains("actual=404"));
    }

    @Test(groups = "smoke")
    public void statusCodeMismatchIsParsed() {
        String insight = FailureInsight.describe(
                new AssertionError("Expected status code <201> but was <500>."),
                "corr-2");
        Assert.assertTrue(insight.contains("expected=201"));
        Assert.assertTrue(insight.contains("actual=500"));
    }

    @Test(groups = "smoke")
    public void transportAndAuthenticationAreClassified() {
        Assert.assertEquals(
                FailureInsight.classification(new IllegalStateException(
                        "HTTP GET /ping failed before a response was received")),
                "transport");
        Assert.assertEquals(
                FailureInsight.classification(new AuthenticationException("POST /auth returned HTTP 500")),
                "authentication");
    }

    @Test(groups = "smoke")
    public void failureTextDoesNotKeepAToken() {
        String insight = FailureInsight.describe(
                new AssertionError("expected [hidden] but found [{\"token\":\"super-secret-token\"}]"),
                "corr-3");
        Assert.assertFalse(insight.contains("super-secret-token"));
        Assert.assertTrue(insight.contains("***"));
    }
}
