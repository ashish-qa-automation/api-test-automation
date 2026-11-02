package com.ashishqa.demo.booker.clients;

import com.ashishqa.api.clients.ApiRequest;
import com.ashishqa.api.clients.RestClient;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Restful Booker {@code POST /auth}. The framework token provider also calls this endpoint;
 * this client exists so tests can assert the login contract directly.
 */
public final class AuthClient {

    public static final String AUTH = "/auth";

    private final RestClient restClient;

    public AuthClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public Response createToken(String username, String password) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("username", username);
        body.put("password", password);
        return restClient.post(ApiRequest.to(AUTH).anonymous().body(body));
    }
}
