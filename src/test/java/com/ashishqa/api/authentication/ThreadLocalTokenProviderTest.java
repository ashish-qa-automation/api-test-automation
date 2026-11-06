package com.ashishqa.api.authentication;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadLocalTokenProviderTest {

    @Test(groups = "smoke")
    public void parallelThreadsDoNotShareTokens() throws Exception {
        AtomicInteger fetches = new AtomicInteger();
        ThreadLocalTokenProvider provider = new ThreadLocalTokenProvider(() -> "t-" + fetches.incrementAndGet());
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<String> first = pool.submit(() -> {
                String token = provider.token();
                Assert.assertEquals(provider.token(), token);
                return token;
            });
            Future<String> second = pool.submit(provider::token);
            Assert.assertNotEquals(first.get(5, TimeUnit.SECONDS), second.get(5, TimeUnit.SECONDS));
            Assert.assertEquals(fetches.get(), 2);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test(groups = "smoke")
    public void invalidateClearsOnlyTheCurrentThread() throws Exception {
        ThreadLocalTokenProvider provider = new ThreadLocalTokenProvider(() -> "fresh-" + UUID.randomUUID());
        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            String otherThread = pool.submit(provider::token).get(5, TimeUnit.SECONDS);
            provider.invalidate();
            String thisThread = provider.token();
            String otherThreadAgain = pool.submit(provider::token).get(5, TimeUnit.SECONDS);
            Assert.assertNotEquals(thisThread, otherThread);
            Assert.assertEquals(otherThreadAgain, otherThread);
        } finally {
            pool.shutdownNow();
        }
    }
}