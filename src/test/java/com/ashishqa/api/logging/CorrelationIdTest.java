package com.ashishqa.api.logging;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CorrelationIdTest {

    @Test(groups = "smoke")
    public void startReplacesTheCurrentThreadsId() {
        CorrelationId.clear();
        try {
            String first = CorrelationId.start();
            Assert.assertEquals(CorrelationId.current(), first);
            String second = CorrelationId.start();
            Assert.assertEquals(CorrelationId.current(), second);
            Assert.assertNotEquals(first, second);
        } finally {
            CorrelationId.clear();
        }
        Assert.assertEquals(CorrelationId.current(), "");
    }

    @Test(groups = "smoke")
    public void anotherThreadDoesNotSeeThisThreadsId() throws Exception {
        String mine = CorrelationId.start();
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<String> other = pool.submit(CorrelationId::current);
            Assert.assertEquals(other.get(5, TimeUnit.SECONDS), "");
            Assert.assertEquals(CorrelationId.current(), mine);
        } finally {
            CorrelationId.clear();
            pool.shutdownNow();
        }
    }
}
