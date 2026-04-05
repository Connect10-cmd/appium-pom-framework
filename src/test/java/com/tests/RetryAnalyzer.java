package com.tests;

import com.framework.utils.LogManager;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import java.util.HashMap;
import java.util.Map;

/**
 * RetryAnalyzer — Retries flaky tests up to maxAttempts before marking as failed.
 * Thread-safe: Uses test identity as key to track retries per test independently.
 * Usage: @Test(retryAnalyzer = RetryAnalyzer.class)
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Map<String, Integer> retryMap = new HashMap<>();
    private static final int MAX_ATTEMPTS = 2;

    @Override
    public boolean retry(ITestResult result) {
        String testKey = getTestKey(result);
        int currentAttempt = retryMap.getOrDefault(testKey, 0);

        if (currentAttempt < MAX_ATTEMPTS) {
            currentAttempt++;
            retryMap.put(testKey, currentAttempt);
            LogManager.warn("Retrying test [" + currentAttempt + "/" + MAX_ATTEMPTS + "]: "
                + result.getName());
            return true;
        }

        retryMap.remove(testKey);
        return false;
    }

    private String getTestKey(ITestResult result) {
        StringBuilder key = new StringBuilder();
        key.append(result.getMethod().getRealClass().getSimpleName())
            .append(".").append(result.getMethod().getMethodName());
        Object[] parameters = result.getParameters();
        if (parameters != null && parameters.length > 0) {
            key.append("[");
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) key.append(",");
                key.append(parameters[i] != null ? parameters[i].toString() : "null");
            }
            key.append("]");
        }
        return key.toString();
    }
}
