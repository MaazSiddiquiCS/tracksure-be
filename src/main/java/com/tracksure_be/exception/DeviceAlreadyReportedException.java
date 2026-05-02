package com.tracksure_be.exception;

/**
 * Exception thrown when attempting to report a device that is already reported as stolen
 */
public class DeviceAlreadyReportedException extends RuntimeException {
    public DeviceAlreadyReportedException(String message) {
        super(message);
    }

    public DeviceAlreadyReportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
