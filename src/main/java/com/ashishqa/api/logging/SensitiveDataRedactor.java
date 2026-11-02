package com.ashishqa.api.logging;

import java.util.regex.Pattern;

/**
 * Masks credentials in logs. Used by HTTP logging now; Allure attachments will reuse it in Phase 9.
 */
public final class SensitiveDataRedactor {

    private static final String MASK = "***";
    private static final Pattern JSON_SECRET_FIELDS = Pattern.compile(
            "(?i)(\"(?:password|token|accessToken|refreshToken|apiKey|authorization|access_token|refresh_token)\"\\s*:\\s*\")([^\"]*)(\")");
    private static final Pattern AUTHORIZATION_HEADER = Pattern.compile(
            "(?i)(Authorization\\s*[:=]\\s*)([^\\r\\n]+)");
    private static final Pattern COOKIE_HEADER = Pattern.compile(
            "(?i)(Cookie\\s*[:=]\\s*)([^\\r\\n]+)");
    private static final Pattern TOKEN_COOKIE = Pattern.compile(
            "(?i)(\\btoken\\s*=\\s*)([^;\\s]+)");

    public String redact(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        String redacted = JSON_SECRET_FIELDS.matcher(raw).replaceAll("$1" + MASK + "$3");
        redacted = AUTHORIZATION_HEADER.matcher(redacted).replaceAll("$1" + MASK);
        redacted = COOKIE_HEADER.matcher(redacted).replaceAll("$1" + MASK);
        redacted = TOKEN_COOKIE.matcher(redacted).replaceAll("$1" + MASK);
        return redacted;
    }
}
