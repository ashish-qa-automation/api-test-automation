package com.ashishqa.api.specifications;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.ResponseSpecification;

/**
 * Optional reusable response expectations. Tests apply these explicitly;
 * {@code RestClient} does not attach them automatically.
 */
public final class ResponseSpecFactory {

    private ResponseSpecFactory() {
    }

    public static ResponseSpecification statusCode(int statusCode) {
        return new ResponseSpecBuilder()
                .expectStatusCode(statusCode)
                .build();
    }

    public static ResponseSpecification jsonWithStatus(int statusCode) {
        return new ResponseSpecBuilder()
                .expectStatusCode(statusCode)
                .expectContentType(ContentType.JSON)
                .build();
    }
}
