package com.framework.utils;

import com.framework.core.CapabilitiesManager;
import com.framework.core.WaitUtils;
import com.framework.utils.LogManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * MobileActions — Production-ready action layer for mobile automation.
 * Handles flakiness, retries, logging, and mobile gestures centrally.
 * Reduces duplicate code in Page classes and provides robust element interactions.
 *
 * Features:
 * - Retry mechanism for stale/intermittent failures
 * - Comprehensive logging (before/after actions)
 * - Automatic screenshot capture on failures
 * - Mobile-specific gestures (swipe, scroll, tap, long press)
 * - Configurable timeouts
 * - Element highlighting for debugging
 */
public class MobileActions {

    private final AppiumDriver driver;
    private final WaitUtils wait;
    private final CapabilitiesManager config;
    private final int MAX_RETRIES;
    private final long RETRY_DELAY_MS;
    private final boolean ENABLE_HIGHLIGHTING;
    private final Set<String> RETRY_EXCEPTIONS;

    public MobileActions(AppiumDriver driver, WaitUtils wait) {
        this.driver = driver;
        this.wait = wait;
        this.config = CapabilitiesManager.getInstance();
        this.MAX_RETRIES = config.getMaxRetryAttempts();
        this.RETRY_DELAY_MS = config.getRetryDelayMs();
        this.ENABLE_HIGHLIGHTING = config.isHighlightingEnabled();
        this.RETRY_EXCEPTIONS = Set.copyOf(config.getRetryExceptions());
    }

    // ── Core Element Actions ──────────────────────────────────────────────────────

    /**
     * Click with retry logic, wait, logging, and highlighting.
     */
    public void click(By locator, String elementName) {
        executeWithRetry(() -> {
            WebElement element = waitForClickable(locator, elementName);
            highlightElement(element);
            element.click();
            LogManager.info("Clicked on " + elementName);
        }, "click on " + elementName);
    }

    public void click(WebElement element, String elementName) {
        executeWithRetry(() -> {
            WebElement clickableElement = wait.waitForClickable(element);
            highlightElement(clickableElement);
            clickableElement.click();
            LogManager.info("Clicked on " + elementName);
        }, "click on " + elementName);
    }

    /**
     * SendKeys with clear, retry, logging, and highlighting.
     */
    public void sendKeys(By locator, String text, String elementName) {
        executeWithRetry(() -> {
            WebElement element = waitForVisible(locator, elementName);
            highlightElement(element);
            element.clear();
            element.sendKeys(text);
            LogManager.info("Entered text into " + elementName);
        }, "sendKeys to " + elementName);
    }

    public void sendKeys(WebElement element, String text, String elementName) {
        executeWithRetry(() -> {
            WebElement inputElement = wait.waitForClickable(element);
            highlightElement(inputElement);
            inputElement.clear();
            inputElement.sendKeys(text);
            LogManager.info("Entered text into " + elementName);
        }, "sendKeys to " + elementName);
    }

    /**
     * Get text with retry and logging.
     */
    public String getText(By locator, String elementName) {
        return executeWithRetryAndReturn(() -> {
            LogManager.info("Attempting to get text from: " + elementName);
            WebElement element = wait.waitForVisible(driver.findElement(locator));
            highlightElement(element);
            String text = element.getText();
            LogManager.info("Retrieved text from " + elementName + ": '" + text + "'");
            return text;
        }, "getText from " + elementName);
    }

    public String getText(WebElement element, String elementName) {
        return executeWithRetryAndReturn(() -> {
            LogManager.info("Attempting to get text from: " + elementName);
            WebElement visibleElement = wait.waitForVisible(element);
            highlightElement(visibleElement);
            String text = visibleElement.getText();
            LogManager.info("Retrieved text from " + elementName + ": '" + text + "'");
            return text;
        }, "getText from " + elementName);
    }

    /**
     * Check if element is displayed with retry.
     */
    public boolean isDisplayed(By locator, String elementName) {
        try {
            return executeWithRetryAndReturn(() -> {
                boolean displayed = driver.findElement(locator).isDisplayed();
                LogManager.debug(elementName + " display status: " + displayed);
                return displayed;
            }, "isDisplayed check on " + elementName);
        } catch (Exception e) {
            LogManager.debug(elementName + " is not displayed: " + e.getMessage());
            return false;
        }
    }

    public boolean isDisplayed(WebElement element, String elementName) {
        try {
            return executeWithRetryAndReturn(() -> {
                boolean displayed = element.isDisplayed();
                LogManager.debug(elementName + " display status: " + displayed);
                return displayed;
            }, "isDisplayed check on " + elementName);
        } catch (Exception e) {
            LogManager.debug(elementName + " is not displayed: " + e.getMessage());
            return false;
        }
    }

    // ── Wait Handling ────────────────────────────────────────────────────────────

    /**
     * Wait for element to be visible.
     */
    public WebElement waitForVisible(By locator, String elementName) {
        LogManager.debug("Waiting for " + elementName + " to be visible");
        WebElement element = wait.waitForVisible(driver.findElement(locator));
        LogManager.debug(elementName + " is now visible");
        return element;
    }

    public WebElement waitForVisible(WebElement element, String elementName) {
        LogManager.info("Waiting for " + elementName + " to be visible");
        WebElement visibleElement = wait.waitForVisible(element);
        LogManager.info(elementName + " is now visible");
        return visibleElement;
    }

    /**
     * Wait for element to be clickable.
     */
    public WebElement waitForClickable(By locator, String elementName) {
        LogManager.debug("Waiting for " + elementName + " to be clickable");
        WebElement element = wait.waitForClickable(driver.findElement(locator));
        LogManager.debug(elementName + " is now clickable");
        return element;
    }

    public WebElement waitForClickable(WebElement element, String elementName) {
        LogManager.debug("Waiting for " + elementName + " to be clickable");
        WebElement clickableElement = wait.waitForClickable(element);
        LogManager.debug(elementName + " is now clickable");
        return clickableElement;
    }

    /**
     * Wait for element presence (not necessarily visible).
     */
    public WebElement waitForPresence(By locator, String elementName) {
        LogManager.debug("Waiting for presence of: " + elementName);
        // Custom wait for presence
        WebElement element = new WebDriverWait(driver, config.getDefaultTimeout())
            .until(ExpectedConditions.presenceOfElementLocated(locator));
        LogManager.info(elementName + " is now present");
        return element;
    }

    // ── Mobile-Specific Actions ──────────────────────────────────────────────────

    /**
     * Swipe in specified direction.
     */
    public void swipe(String direction, String context) {
        executeWithRetry(() -> {
            LogManager.info("Performing swipe " + direction + " in context: " + context);
            Dimension size = driver.manage().window().getSize();
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");

            Sequence swipe = null;
            switch (direction.toLowerCase()) {
                case "up":
                    swipe = createSwipeSequence(finger, size.width / 2, size.height * 3 / 4,
                                               size.width / 2, size.height / 4);
                    break;
                case "down":
                    swipe = createSwipeSequence(finger, size.width / 2, size.height / 4,
                                               size.width / 2, size.height * 3 / 4);
                    break;
                case "left":
                    swipe = createSwipeSequence(finger, size.width * 3 / 4, size.height / 2,
                                               size.width / 4, size.height / 2);
                    break;
                case "right":
                    swipe = createSwipeSequence(finger, size.width / 4, size.height / 2,
                                               size.width * 3 / 4, size.height / 2);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid swipe direction: " + direction);
            }

            driver.perform(List.of(swipe));
            LogManager.info("Swipe " + direction + " completed successfully");
        }, "swipe " + direction);
    }

    private Sequence createSwipeSequence(PointerInput finger, int startX, int startY, int endX, int endY) {
        return new Sequence(finger, 1)
            .addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY))
            .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
            .addAction(finger.createPointerMove(Duration.ofMillis(600), PointerInput.Origin.viewport(), endX, endY))
            .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
    }

    /**
     * Scroll to element by text (platform-specific - override in subclasses).
     */
    public void scrollToElement(String text, String context) {
        throw new UnsupportedOperationException("Scroll to element not implemented for this platform. Use AndroidActions or IOSActions.");
    }

    /**
     * Tap by coordinates.
     */
    public void tapByCoordinates(int x, int y, String context) {
        executeWithRetry(() -> {
            LogManager.info("Tapping at coordinates (" + x + "," + y + ") in context: " + context);
            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence tap = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(List.of(tap));
            LogManager.info("Tap at coordinates completed successfully");
        }, "tap at coordinates (" + x + "," + y + ")");
    }

    /**
     * Long press on element.
     */
    public void longPress(By locator, String elementName) {
        executeWithRetry(() -> {
            LogManager.info("Performing long press on: " + elementName);
            WebElement element = wait.waitForClickable(driver.findElement(locator));
            highlightElement(element);
            Point loc = element.getLocation();
            Dimension size = element.getSize();
            int cx = loc.x + size.width / 2;
            int cy = loc.y + size.height / 2;

            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence press = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), cx, cy))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(1500), PointerInput.Origin.viewport(), cx, cy))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(List.of(press));
            LogManager.info("Long press on " + elementName + " completed successfully");
        }, "long press on " + elementName);
    }

    public void longPress(WebElement element, String elementName) {
        executeWithRetry(() -> {
            LogManager.info("Performing long press on: " + elementName);
            WebElement clickableElement = wait.waitForClickable(element);
            highlightElement(clickableElement);
            Point loc = clickableElement.getLocation();
            Dimension size = clickableElement.getSize();
            int cx = loc.x + size.width / 2;
            int cy = loc.y + size.height / 2;

            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence press = new Sequence(finger, 1)
                .addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), cx, cy))
                .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                .addAction(finger.createPointerMove(Duration.ofMillis(1500), PointerInput.Origin.viewport(), cx, cy))
                .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
            driver.perform(List.of(press));
            LogManager.info("Long press on " + elementName + " completed successfully");
        }, "long press on " + elementName);
    }

    // ── Robustness & Retry Mechanism ─────────────────────────────────────────────

    /**
     * Check if exception should trigger retry based on config.
     */
    private boolean shouldRetry(Exception e) {
        String exceptionName = e.getClass().getSimpleName();
        return RETRY_EXCEPTIONS.contains(exceptionName);
    }

    /**
     * Execute action with retry logic for flakiness handling.
     */
    protected void executeWithRetry(Runnable action, String actionDescription) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                action.run();
                return; // Success
            } catch (Exception e) {
                if (shouldRetry(e) && attempt < MAX_RETRIES) {
                    lastException = e;
                    LogManager.warn("Attempt " + attempt + " failed for " + actionDescription +
                        ". Retrying... (" + e.getClass().getSimpleName() + ")");
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry", ie);
                    }
                } else {
                    // For non-retryable exceptions or last attempt, capture screenshot and rethrow
                    captureScreenshotOnFailure(actionDescription);
                    throw e;
                }
            }
        }
        // All retries failed
        captureScreenshotOnFailure(actionDescription);
        LogManager.error("All " + MAX_RETRIES + " attempts failed for " + actionDescription, lastException);
        throw new RuntimeException("Failed to perform action after " + MAX_RETRIES + " attempts: " + actionDescription, lastException);
    }

    /**
     * Execute function with retry and return value.
     */
    private <T> T executeWithRetryAndReturn(java.util.function.Supplier<T> function, String actionDescription) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return function.get();
            } catch (Exception e) {
                if (shouldRetry(e) && attempt < MAX_RETRIES) {
                    lastException = e;
                    LogManager.warn("Attempt " + attempt + " failed for " + actionDescription +
                        ". Retrying... (" + e.getClass().getSimpleName() + ")");
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry", ie);
                    }
                } else {
                    captureScreenshotOnFailure(actionDescription);
                    throw e;
                }
            }
        }
        captureScreenshotOnFailure(actionDescription);
        LogManager.error("All " + MAX_RETRIES + " attempts failed for " + actionDescription, lastException);
        throw new RuntimeException("Failed to perform action after " + MAX_RETRIES + " attempts: " + actionDescription, lastException);
    }

    // ── Logging & Screenshot ─────────────────────────────────────────────────────

    /**
     * Capture screenshot on failure.
     */
    private void captureScreenshotOnFailure(String context) {
        try {
            TakesScreenshot screenshot = (TakesScreenshot) driver;
            byte[] screenshotBytes = screenshot.getScreenshotAs(OutputType.BYTES);
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "failure_" + context.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".png";
            String screenshotDir = System.getProperty("screenshot.dir", "target/screenshots");
            Files.createDirectories(Paths.get(screenshotDir));
            String filePath = screenshotDir + File.separator + fileName;
            Files.write(Paths.get(filePath), screenshotBytes);
            LogManager.info("Screenshot captured on failure: " + filePath);
        } catch (Exception e) {
            LogManager.error("Failed to capture screenshot", e);
        }
    }

    // ── Element Highlighting ─────────────────────────────────────────────────────

    /**
     * Highlight element for debugging (optional).
     */
    private void highlightElement(WebElement element) {
        if (!ENABLE_HIGHLIGHTING) return;

        try {
            // Use JavaScript to highlight element
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].style.border='3px solid red'", element);
            Thread.sleep(100); // Brief highlight
            js.executeScript("arguments[0].style.border=''", element);
        } catch (Exception e) {
            // Ignore highlighting failures
        }
    }

    // ── Utility Methods ──────────────────────────────────────────────────────────

    /**
     * Mask sensitive data in logs.
     */
    private String maskSensitiveData(String text) {
        if (text == null) return "null";
        if (text.length() > 10) {
            return text.substring(0, 3) + "***" + text.substring(text.length() - 3);
        }
        return "***";
    }
}