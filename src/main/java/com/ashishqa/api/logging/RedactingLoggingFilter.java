package com.ashishqa.api.logging;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Logs request and response text after {@link SensitiveDataRedactor} has masked secrets.
 */
public final class RedactingLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RedactingLoggingFilter.class);

    private final SensitiveDataRedactor redactor;
    private final boolean logRequest;
    private final boolean logResponse;

    public RedactingLoggingFilter(SensitiveDataRedactor redactor, boolean logRequest, boolean logResponse) {
        this.redactor = Objects.requireNonNull(redactor, "redactor");
        this.logRequest = logRequest;
        this.logResponse = logResponse;
    }

    @Override
    public Response filter(
            FilterableRequestSpecification requestSpec,
            FilterableResponseSpecification responseSpec,
            FilterContext ctx) {
        if (logRequest) {
            log.info(
                    "correlationId={} HTTP request {} {}",
                    CorrelationId.current(),
                    requestSpec.getMethod(),
                    requestSpec.getURI());
            log.debug(
                    "correlationId={} headers={} cookies={} body={}",
                    CorrelationId.current(),
                    redactor.redact(String.valueOf(requestSpec.getHeaders())),
                    redactor.redact(String.valueOf(requestSpec.getCookies())),
                    redactor.redact(bodyOf(requestSpec)));
        }
        Response response = ctx.next(requestSpec, responseSpec);
        if (logResponse) {
            log.info(
                    "correlationId={} HTTP response {} {} -> {}",
                    CorrelationId.current(),
                    requestSpec.getMethod(),
                    requestSpec.getURI(),
                    response.statusCode());
            log.debug(
                    "correlationId={} body={}",
                    CorrelationId.current(),
                    redactor.redact(response.asString()));
        }
        return response;
    }

    private static String bodyOf(FilterableRequestSpecification requestSpec) {
        Object body = requestSpec.getBody();
        return body == null ? "" : String.valueOf(body);
    }
}
