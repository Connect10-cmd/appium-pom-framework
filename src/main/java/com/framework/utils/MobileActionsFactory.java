package com.framework.utils;

import com.framework.core.WaitUtils;
import io.appium.java_client.AppiumDriver;

/**
 * MobileActionsFactory — Creates platform-specific action instances.
 */
public class MobileActionsFactory {

    public static MobileActions getActions(String platform, AppiumDriver driver, WaitUtils wait) {
        switch (platform.toLowerCase()) {
            case "android":
                return new AndroidActions(driver, wait);
            case "ios":
                return new IOSActions(driver, wait);
            default:
                return new MobileActions(driver, wait);
        }
    }
}