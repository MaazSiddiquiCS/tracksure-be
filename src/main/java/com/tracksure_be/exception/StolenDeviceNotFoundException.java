package com.tracksure_be.exception;

/**
 * Exception thrown when a stolen device is not found
 */
public class StolenDeviceNotFoundException extends RuntimeException {
    public StolenDeviceNotFoundException(String message) {
        super(message);
    }

    public StolenDeviceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
