package com.ashishqa.api.authentication;

import com.ashishqa.api.clients.ApiRequest;
import com.ashishqa.api.clients.RestClient;
import io.restassured.response.Response;

import java.util.Map;
import java.util.Objects;

/**
 * Obtains a token from a JSON login endpoint, for example Restful Booker {@code POST /auth}.
 */
public final class JsonTokenSource implements TokenSource {

    private final RestClient restClient;
    private final String endpoint;
    private final String tokenField;
    private final Map<String, String> body;

    public JsonTokenSource(RestClient restClient, String endpoint, String tokenField, Map<String, String> body) {
        this.restClient = Objects.requireNonNull(restClient, "restClient");
        this.endpoint = requireText("endpoint", endpoint);
        this.tokenField = tokenField == null || tokenField.isBlank() ? "token" : tokenField;
        this.body = Map.copyOf(Objects.requireNonNull(body, "body"));
    }

    @Override
    public String fetch() {
        Response response = restClient.post(ApiRequest.to(endpoint).anonymous().body(body));
        if (response.statusCode() != 200) {
            throw new AuthenticationException(
                    "Token request " + endpoint + " returned HTTP " + response.statusCode());
        }
        String token = response.jsonPath().getString(tokenField);
        if (token == null || token.isBlank()) {
            throw new AuthenticationException("Auth response did not contain field '" + tokenField + "'");
        }
        return token;
    }

    private static String requireText(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new AuthenticationException(name + " is required");
        }
        return value;
    }
}
