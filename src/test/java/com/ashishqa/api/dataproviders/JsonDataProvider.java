package com.ashishqa.api.dataproviders;

import com.ashishqa.api.serialization.JsonCodec;

/**
 * Loads a JSON array from the test classpath into a TestNG {@code Object[][]}.
 */
public final class JsonDataProvider {

    private JsonDataProvider() {
    }

    public static <T> Object[][] array(String classpathLocation, Class<T[]> type) {
        T[] items = JsonCodec.instance().fromResource(classpathLocation, type);
        Object[][] rows = new Object[items.length][1];
        for (int i = 0; i < items.length; i++) {
            rows[i][0] = items[i];
        }
        return rows;
    }
}
