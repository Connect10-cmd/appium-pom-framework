package com.framework.utils;

import com.framework.core.WaitUtils;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.NoSuchElementException;

/**
 * ActionWrapper — Production-grade wrapper for all element interactions.
 * Provides logging, retry logic, and exception handling for flaky elements.
 * All page actions should route through here instead of direct WebElement calls.
 */
public class ActionWrapper {

    private final AppiumDriver driver;
    private final WaitUtils wait;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 500;

    public ActionWrapper(AppiumDriver driver, WaitUtils wait) {
        this.driver = driver;
        this.wait = wait;
    }

    /**
     * Click with retry logic and comprehensive logging.
     * Handles StaleElementReferenceException automatically.
     */
    public void click(WebElement element, String elementName) {
        LogManager.info("Attempting to click: " + elementName);
        retryAction(() -> {
            WebElement clickableElement = wait.waitForClickable(element);
            clickableElement.click();
            LogManager.info("Successfully clicked: " + elementName);
        }, "click on " + elementName);
    }

    /**
     * SendKeys with retry logic and logging.
     * Clears field first, then types text.
     */
    public void sendKeys(WebElement element, String text, String elementName) {
        LogManager.info("Attempting to type '" + maskSensitiveData(text) + "' into: " + elementName);
        retryAction(() -> {
            WebElement inputElement = wait.waitForClickable(element);
            inputElement.clear();
            inputElement.sendKeys(text);
            LogManager.info("Successfully typed into: " + elementName);
        }, "sendKeys to " + elementName);
    }

    /**
     * Get text with retry logic.
     */
    public String getText(WebElement element, String elementName) {
        LogManager.info("Attempting to get text from: " + elementName);
        return retryFunction(() -> {
            WebElement visibleElement = wait.waitForVisible(element);
            String text = visibleElement.getText();
            LogManager.info("Retrieved text from " + elementName + ": '" + text + "'");
            return text;
        }, "getText from " + elementName);
    }

    /**
     * Check if element is displayed with retry logic.
     */
    public boolean isDisplayed(WebElement element, String elementName) {
        try {
            boolean displayed = retryFunction(() -> {
                try {
                    return element.isDisplayed();
                } catch (StaleElementReferenceException e) {
                    return false; // Element is stale, consider it not displayed
                }
            }, "isDisplayed check on " + elementName);
            LogManager.debug(elementName + " display status: " + displayed);
            return displayed;
        } catch (Exception e) {
            LogManager.debug(elementName + " is not displayed (exception): " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if element is enabled.
     */
    public boolean isEnabled(WebElement element, String elementName) {
        boolean enabled = retryFunction(() -> {
            WebElement visibleElement = wait.waitForVisible(element);
            return visibleElement.isEnabled();
        }, "isEnabled check on " + elementName);
        LogManager.debug(elementName + " enabled status: " + enabled);
        return enabled;
    }

    /**
     * Wait for element to be visible.
     */
    public WebElement waitForVisible(WebElement element, String elementName) {
        LogManager.info("Waiting for " + elementName + " to be visible");
        WebElement visibleElement = wait.waitForVisible(element);
        LogManager.info(elementName + " is now visible");
        return visibleElement;
    }

    /**
     * Wait for element to be clickable.
     */
    public WebElement waitForClickable(WebElement element, String elementName) {
        LogManager.info("Waiting for " + elementName + " to be clickable");
        WebElement clickableElement = wait.waitForClickable(element);
        LogManager.info(elementName + " is now clickable");
        return clickableElement;
    }

    /**
     * Generic retry mechanism for actions that may fail due to flakiness.
     */
    private void retryAction(Runnable action, String actionDescription) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                action.run();
                return; // Success
            } catch (StaleElementReferenceException | NoSuchElementException e) {
                lastException = e;
                if (attempt < MAX_RETRIES) {
                    LogManager.warn("Attempt " + attempt + " failed for " + actionDescription +
                        ". Retrying... (" + e.getClass().getSimpleName() + ")");
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry", ie);
                    }
                }
            }
        }
        // All retries failed
        LogManager.error("All " + MAX_RETRIES + " attempts failed for " + actionDescription, lastException);
        throw new RuntimeException("Failed to perform action after " + MAX_RETRIES + " attempts: " + actionDescription, lastException);
    }

    /**
     * Generic retry mechanism for functions that return values.
     */
    private <T> T retryFunction(java.util.function.Supplier<T> function, String actionDescription) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return function.get();
            } catch (StaleElementReferenceException | NoSuchElementException e) {
                lastException = e;
                if (attempt < MAX_RETRIES) {
                    LogManager.warn("Attempt " + attempt + " failed for " + actionDescription +
                        ". Retrying... (" + e.getClass().getSimpleName() + ")");
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry", ie);
                    }
                }
            }
        }
        // All retries failed
        LogManager.error("All " + MAX_RETRIES + " attempts failed for " + actionDescription, lastException);
        throw new RuntimeException("Failed to perform action after " + MAX_RETRIES + " attempts: " + actionDescription, lastException);
    }

    /**
     * Mask sensitive data in logs (passwords, tokens, etc.)
     */
    private String maskSensitiveData(String text) {
        if (text == null) return "null";
        if (text.length() > 10) {
            return text.substring(0, 3) + "***" + text.substring(text.length() - 3);
        }
        return "***"; // For short sensitive data
    }
}