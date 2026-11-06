package com.ashishqa.api.listeners;

import com.ashishqa.api.logging.CorrelationId;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

/**
 * Records a failed test while Allure still has that test open.
 * Allure's own listener closes the test case in {@code onTestFailure}, which runs later.
 */
public final class FailureInsightListener implements IInvokedMethodListener {

    private static final Logger log = LoggerFactory.getLogger(FailureInsightListener.class);

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult result) {
        if (method == null || !method.isTestMethod() || result == null || result.getThrowable() == null) {
            return;
        }
        if (result.getStatus() != ITestResult.FAILURE) {
            return;
        }
        String insight = FailureInsight.describe(result.getThrowable(), CorrelationId.current());
        log.error("Test failed {}{}{}", result.getMethod().getQualifiedName(), System.lineSeparator(), insight);
        Allure.addAttachment("Failure", "text/plain", insight, ".txt");
    }
}
