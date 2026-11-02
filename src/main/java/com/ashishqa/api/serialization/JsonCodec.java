package com.ashishqa.api.serialization;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * Shared Jackson mapper. The instance is immutable after construction and safe to reuse across threads.
 */
public final class JsonCodec {

    private static final JsonCodec INSTANCE = new JsonCodec();

    private final ObjectMapper mapper;

    private JsonCodec() {
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public static JsonCodec instance() {
        return INSTANCE;
    }

    public ObjectMapper mapper() {
        return mapper;
    }

    public String toJson(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to serialize " + typeName(value), ex);
        }
    }

    public <T> T fromJson(String json, Class<T> type) {
        Objects.requireNonNull(json, "json");
        Objects.requireNonNull(type, "type");
        try {
            return mapper.readValue(json, type);
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to deserialize " + type.getName(), ex);
        }
    }

    public <T> T fromResource(String classpathLocation, Class<T> type) {
        Objects.requireNonNull(classpathLocation, "classpathLocation");
        Objects.requireNonNull(type, "type");
        String resource = classpathLocation.startsWith("/") ? classpathLocation.substring(1) : classpathLocation;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = JsonCodec.class.getClassLoader();
        }
        try (InputStream in = classLoader.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Missing classpath resource: " + resource);
            }
            return mapper.readValue(in, type);
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to read classpath resource: " + resource, ex);
        }
    }

    private static String typeName(Object value) {
        return value == null ? "null" : value.getClass().getName();
    }
}
