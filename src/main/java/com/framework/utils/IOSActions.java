package com.framework.utils;

import com.framework.core.WaitUtils;
import com.framework.utils.LogManager;
import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.ios.IOSDriver;

/**
 * IOSActions — Platform-specific actions for iOS.
 * Extends MobileActions with iOS-optimized gestures.
 */
public class IOSActions extends MobileActions {
    private final IOSDriver iosDriver;

    public IOSActions(AppiumDriver driver, WaitUtils wait) {
        super(driver, wait);
        this.iosDriver = (IOSDriver) driver;
    }

    @Override
    public void scrollToElement(String text, String context) {
        executeWithRetry(() -> {
            LogManager.debug("iOS scroll to '" + text + "' in " + context);
            iosDriver.findElement(AppiumBy.iOSNsPredicateString("label == '" + text + "' OR name == '" + text + "'"));
            LogManager.debug("Successfully found '" + text + "' on iOS");
        }, "iOS scroll to " + text);
    }

    @Override
    public void swipe(String direction, String context) {
        executeWithRetry(() -> {
            LogManager.debug("iOS swipe " + direction + " in " + context);
            // iOS-specific swipe implementation
            super.swipe(direction, context); // Use base implementation
        }, "iOS swipe " + direction);
    }
}