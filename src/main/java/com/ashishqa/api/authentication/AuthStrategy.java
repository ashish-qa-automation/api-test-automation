package com.ashishqa.api.authentication;

import io.restassured.specification.RequestSpecification;

/**
 * Applies one authentication mechanism to a REST Assured request specification.
 */
public interface AuthStrategy {

    void apply(RequestSpecification spec);
}
