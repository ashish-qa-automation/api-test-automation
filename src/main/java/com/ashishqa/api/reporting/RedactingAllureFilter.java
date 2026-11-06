package com.ashishqa.api.reporting;

import com.ashishqa.api.logging.CorrelationId;
import com.ashishqa.api.logging.SensitiveDataRedactor;
import io.qameta.allure.attachment.DefaultAttachmentProcessor;
import io.qameta.allure.attachment.FreemarkerAttachmentRenderer;
import io.qameta.allure.attachment.http.HttpRequestAttachment;
import io.qameta.allure.attachment.http.HttpResponseAttachment;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.filter.FilterContext;
import io.restassured.http.Cookie;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Allure HTTP attachments with the same redactor as the log filter.
 * {@link AllureRestAssured} writes cookie values and JSON secrets before any hook can mask them,
 * so this override builds the attachments itself and does not call {@code super.filter}.
 */
public final class RedactingAllureFilter extends AllureRestAssured {

    private static final Set<String> SENSITIVE_HEADERS = Set.of(
            "authorization", "cookie", "set-cookie", "proxy-authorization", "x-api-key");

    private final SensitiveDataRedactor redactor;

    public RedactingAllureFilter(SensitiveDataRedactor redactor) {
        this.redactor = Objects.requireNonNull(redactor, "redactor");
    }

    @Override
    public Response filter(
            FilterableRequestSpecification requestSpec,
            FilterableResponseSpecification responseSpec,
            FilterContext ctx) {
        String correlationId = CorrelationId.current();
        String requestName = correlationId.isBlank() ? "Request" : "Request " + correlationId;
        HttpRequestAttachment requestAttachment = HttpRequestAttachment.Builder.create(requestName, requestSpec.getURI())
                .setMethod(requestSpec.getMethod())
                .setHeaders(headers(requestSpec.getHeaders(), redactor))
                .setCookies(cookies(requestSpec.getCookies(), redactor))
                .setBody(redactor.redact(bodyOf(requestSpec.getBody())))
                .build();
        new DefaultAttachmentProcessor().addAttachment(requestAttachment, new FreemarkerAttachmentRenderer("http-request.ftl"));

        Response response = ctx.next(requestSpec, responseSpec);

        String responseName = response.getStatusLine();
        if (!correlationId.isBlank()) {
            responseName = responseName + " " + correlationId;
        }
        HttpResponseAttachment responseAttachment = HttpResponseAttachment.Builder.create(responseName)
                .setResponseCode(response.getStatusCode())
                .setHeaders(headers(response.getHeaders(), redactor))
                .setBody(redactor.redact(response.asString()))
                .build();
        new DefaultAttachmentProcessor().addAttachment(
                responseAttachment, new FreemarkerAttachmentRenderer("http-response.ftl"));
        return response;
    }

    private static Map<String, String> headers(Iterable<Header> headers, SensitiveDataRedactor redactor) {
        Map<String, String> values = new LinkedHashMap<>();
        if (headers == null) {
            return values;
        }
        for (Header header : headers) {
            values.put(header.getName(), redactHeader(redactor, header.getName(), header.getValue()));
        }
        return values;
    }

    private static Map<String, String> cookies(Iterable<Cookie> cookies, SensitiveDataRedactor redactor) {
        Map<String, String> values = new LinkedHashMap<>();
        if (cookies == null) {
            return values;
        }
        for (Cookie cookie : cookies) {
            values.put(cookie.getName(), redactHeader(redactor, "cookie", cookie.getName() + "=" + cookie.getValue()));
        }
        return values;
    }

    private static String redactHeader(SensitiveDataRedactor redactor, String name, String value) {
        if (name != null && SENSITIVE_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
            return "***";
        }
        return redactor.redact(value);
    }

    private static String bodyOf(Object body) {
        return body == null ? "" : String.valueOf(body);
    }
}
