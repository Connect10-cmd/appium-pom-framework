package com.framework.utils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

/**
 * LogManager — Thread-aware logging using Log4j2.
 * Thread name is included in every log line for parallel test debugging.
 */
public class LogManager {

    private static final Logger logger =
        org.apache.logging.log4j.LogManager.getLogger(LogManager.class);

    private LogManager() { /* Static utility class */ }

    public static void info(String message) {
        logger.info("[{}] {}", getThread(), message);
    }

    public static void error(String message, Throwable t) {
        if (t != null) {
            logger.error("[{}] {}", getThread(), message, t);
        } else {
            logger.error("[{}] {}", getThread(), message);
        }
    }

    public static void warn(String message) {
        logger.warn("[{}] {}", getThread(), message);
    }

    public static void debug(String message) {
        logger.debug("[{}] {}", getThread(), message);
    }

    public static void setTestName(String name) {
        ThreadContext.put("testName", name);
    }

    public static void clearContext() {
        ThreadContext.clearAll();
    }

    private static String getThread() {
        return Thread.currentThread().getName();
    }

    public static io.opentelemetry.api.logs.Logger getLogger(Class<IOSActions> class1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLogger'");
    }
}
