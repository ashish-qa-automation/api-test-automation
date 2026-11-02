package com.ashishqa.api.clients;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Per-call HTTP details. Application clients build this; {@link RestClient} sends it.
 */
public final class ApiRequest {

    private final String path;
    private final Map<String, Object> pathParams = new LinkedHashMap<>();
    private final Map<String, Object> queryParams = new LinkedHashMap<>();
    private final Map<String, String> headers = new LinkedHashMap<>();
    private Object body;
    private boolean anonymous;

    private ApiRequest(String path) {
        this.path = Objects.requireNonNull(path, "path");
    }

    public static ApiRequest to(String path) {
        return new ApiRequest(path);
    }

    public ApiRequest pathParam(String name, Object value) {
        pathParams.put(requireName(name), Objects.requireNonNull(value, "path param value"));
        return this;
    }

    public ApiRequest queryParam(String name, Object value) {
        queryParams.put(requireName(name), Objects.requireNonNull(value, "query param value"));
        return this;
    }

    public ApiRequest header(String name, String value) {
        headers.put(requireName(name), Objects.requireNonNull(value, "header value"));
        return this;
    }

    public ApiRequest body(Object body) {
        this.body = body;
        return this;
    }

    /**
     * Do not apply the RestClient {@link com.ashishqa.api.authentication.AuthStrategy}.
     * Used for login calls and negative unauthorized tests.
     */
    public ApiRequest anonymous() {
        this.anonymous = true;
        return this;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public String path() {
        return path;
    }

    public Map<String, Object> pathParams() {
        return Collections.unmodifiableMap(pathParams);
    }

    public Map<String, Object> queryParams() {
        return Collections.unmodifiableMap(queryParams);
    }

    public Map<String, String> headers() {
        return Collections.unmodifiableMap(headers);
    }

    public Object body() {
        return body;
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        return name;
    }
}
