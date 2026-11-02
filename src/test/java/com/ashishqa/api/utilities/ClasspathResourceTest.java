package com.ashishqa.api.utilities;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ClasspathResourceTest {

    @Test(groups = "smoke")
    public void requireReturnsExistingResourcePath() {
        Assert.assertEquals(
                ClasspathResource.require("schemas/booker/booking.json"),
                "schemas/booker/booking.json");
    }

    @Test(groups = "smoke")
    public void requireStripsLeadingSlash() {
        Assert.assertEquals(
                ClasspathResource.require("/schemas/booker/booking.json"),
                "schemas/booker/booking.json");
    }

    @Test(groups = "smoke")
    public void missingResourceFailsClearly() {
        try {
            ClasspathResource.require("schemas/booker/missing.json");
            Assert.fail("Expected missing resource to fail");
        } catch (IllegalStateException ex) {
            Assert.assertTrue(ex.getMessage().contains("schemas/booker/missing.json"));
        }
    }
}
