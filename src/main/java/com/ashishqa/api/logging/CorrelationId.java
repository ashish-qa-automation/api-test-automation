package com.ashishqa.api.logging;

import java.util.UUID;

/**
 * One id per test thread. HTTP logs and Allure attachments read it; they do not share a mutable spec.
 */
public final class CorrelationId {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private CorrelationId() {
    }

    public static String start() {
        String id = UUID.randomUUID().toString();
        CURRENT.set(id);
        return id;
    }

    public static String current() {
        String id = CURRENT.get();
        return id == null ? "" : id;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
