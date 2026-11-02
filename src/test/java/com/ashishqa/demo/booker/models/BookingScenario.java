package com.ashishqa.demo.booker.models;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * One JSON-driven case: a human-readable name, the HTTP status Restful Booker actually returns,
 * and a payload that may omit fields.
 */
public record BookingScenario(String scenario, int expectedStatus, JsonNode payload) {
}
