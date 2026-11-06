package com.ashishqa.api.listeners;

import com.ashishqa.api.authentication.AuthenticationException;
import com.ashishqa.api.logging.SensitiveDataRedactor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns a test failure into a short diagnosis: classification, and expected versus actual when the
 * message has that shape. The text is redacted before it is logged or attached.
 */
public final class FailureInsight {

    private static final Pattern TESTNG_EQUALS = Pattern.compile(
            "expected \\[(.*?)] but found \\[(.*?)]", Pattern.DOTALL);
    private static final Pattern STATUS_CODE = Pattern.compile(
            "Expected status code <([^>]+)> but was <([^>]+)>");
    private static final SensitiveDataRedactor REDACTOR = new SensitiveDataRedactor();

    private FailureInsight() {
    }

    public static String describe(Throwable thrown, String correlationId) {
        Throwable failure = thrown == null ? new IllegalStateException("no throwable") : thrown;
        String message = failure.getMessage() == null ? "" : failure.getMessage();
        StringBuilder text = new StringBuilder();
        text.append("classification=").append(classification(failure)).append(System.lineSeparator());
        text.append("correlationId=").append(correlationId == null ? "" : correlationId).append(System.lineSeparator());
        text.append("exception=").append(failure.getClass().getName()).append(System.lineSeparator());
        appendComparison(text, message);
        text.append("message=").append(message);
        return REDACTOR.redact(text.toString());
    }

    public static String classification(Throwable thrown) {
        Throwable current = thrown;
        while (current != null) {
            if (current instanceof AssertionError) {
                return "assertion";
            }
            if (current instanceof AuthenticationException) {
                return "authentication";
            }
            String message = current.getMessage() == null ? "" : current.getMessage();
            if (current instanceof IllegalStateException && message.contains("failed before a response was received")) {
                return "transport";
            }
            current = current.getCause();
        }
        return "unexpected";
    }

    private static void appendComparison(StringBuilder text, String message) {
        Matcher equals = TESTNG_EQUALS.matcher(message);
        if (equals.find()) {
            text.append("expected=").append(equals.group(1)).append(System.lineSeparator());
            text.append("actual=").append(equals.group(2)).append(System.lineSeparator());
            return;
        }
        Matcher status = STATUS_CODE.matcher(message);
        if (status.find()) {
            text.append("expected=").append(status.group(1)).append(System.lineSeparator());
            text.append("actual=").append(status.group(2)).append(System.lineSeparator());
        }
    }
}
