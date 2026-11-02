package com.ashishqa.api.clients;

import com.ashishqa.api.authentication.AuthStrategy;
import com.ashishqa.api.authentication.NoAuth;
import com.ashishqa.api.config.FrameworkConfig;
import com.ashishqa.api.specifications.RequestSpecFactory;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Instance-based HTTP sender. Returns the raw response; it does not assert business outcomes.
 */
public final class RestClient {

    private static final Logger log = LoggerFactory.getLogger(RestClient.class);

    private final RequestSpecFactory requestSpecFactory;
    private final AuthStrategy authStrategy;

    public RestClient(RequestSpecFactory requestSpecFactory, AuthStrategy authStrategy) {
        this.requestSpecFactory = Objects.requireNonNull(requestSpecFactory, "requestSpecFactory");
        this.authStrategy = authStrategy == null ? NoAuth.INSTANCE : authStrategy;
    }

    public RestClient(FrameworkConfig config) {
        this(new RequestSpecFactory(config), NoAuth.INSTANCE);
    }

    public RestClient(FrameworkConfig config, AuthStrategy authStrategy) {
        this(new RequestSpecFactory(config), authStrategy);
    }

    public Response get(String path) {
        return get(ApiRequest.to(path));
    }

    public Response get(ApiRequest request) {
        return send(Method.GET, request);
    }

    public Response post(ApiRequest request) {
        return send(Method.POST, request);
    }

    public Response put(ApiRequest request) {
        return send(Method.PUT, request);
    }

    public Response patch(ApiRequest request) {
        return send(Method.PATCH, request);
    }

    public Response delete(ApiRequest request) {
        return send(Method.DELETE, request);
    }

    public Response delete(String path) {
        return delete(ApiRequest.to(path));
    }

    private Response send(Method method, ApiRequest request) {
        Objects.requireNonNull(request, "request");
        RequestSpecification spec = RestAssured.given().spec(requestSpecFactory.create());
        if (!request.isAnonymous()) {
            authStrategy.apply(spec);
        }
        if (!request.headers().isEmpty()) {
            spec.headers(request.headers());
        }
        if (!request.pathParams().isEmpty()) {
            spec.pathParams(request.pathParams());
        }
        if (!request.queryParams().isEmpty()) {
            spec.queryParams(request.queryParams());
        }
        if (request.body() != null) {
            spec.contentType(ContentType.JSON).body(request.body());
        }

        log.info("{} {}", method, request.path());
        try {
            Response response = spec.request(method, request.path());
            log.info("{} {} -> {}", method, request.path(), response.statusCode());
            return response;
        } catch (RuntimeException ex) {
            throw new IllegalStateException(
                    "HTTP " + method + " " + request.path() + " failed before a response was received", ex);
        }
    }
}
