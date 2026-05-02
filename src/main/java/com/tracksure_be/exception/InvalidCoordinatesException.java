package com.tracksure_be.exception;

/**
 * Exception thrown when invalid coordinates are provided
 */
public class InvalidCoordinatesException extends RuntimeException {
    public InvalidCoordinatesException(String message) {
        super(message);
    }

    public InvalidCoordinatesException(String message, Throwable cause) {
        super(message, cause);
    }
}
