package com.ashishqa.api.utilities;

import java.util.Objects;

/**
 * Resolves a classpath location and fails clearly when the file is missing.
 */
public final class ClasspathResource {

    private ClasspathResource() {
    }

    public static String require(String location) {
        Objects.requireNonNull(location, "location");
        String resource = location.startsWith("/") ? location.substring(1) : location;
        if (resource.isBlank()) {
            throw new IllegalArgumentException("classpath location is required");
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ClasspathResource.class.getClassLoader();
        }
        if (classLoader.getResource(resource) == null) {
            throw new IllegalStateException("Missing classpath resource: " + resource);
        }
        return resource;
    }
}
