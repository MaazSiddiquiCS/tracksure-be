package com.tracksure_be.exception;

import com.tracksure_be.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler{

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                LocalDateTime.now(),
                request.getRequestURI(),
                "An unexpected error occurred"
        );
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException ex,
            HttpServletRequest request) {

        String detailedMessage = ex.getMostSpecificCause().getMessage();
        String userFriendlyMessage = "A data integrity constraint was violated. This often means a required field is missing, or a unique value (like an email or roll number) is a duplicate.";

        // Optionally, you can try to parse the message to determine the specific constraint
        if (detailedMessage != null) {
            if (detailedMessage.toLowerCase().contains("unique")) {
                userFriendlyMessage = "A value you entered (e.g., email, roll number) already exists in the system.";
            } else if (detailedMessage.toLowerCase().contains("null")) {
                userFriendlyMessage = "A required field was left empty.";
            }
        }

        ApiError error = new ApiError(
                HttpStatus.CONFLICT, // Use 409 CONFLICT for business rule violations
                userFriendlyMessage,
                LocalDateTime.now(),
                request.getRequestURI(),
                "Data Constraint Violation"
        );
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(
            org.springframework.web.bind.MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Extract all field errors into a single, clean message
        String validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining("; "));

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST, // Use 400 Bad Request for client-side validation errors
                validationErrors,
                LocalDateTime.now(),
                request.getRequestURI(),
                "Input validation failed"
        );
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        ApiError error = new ApiError(HttpStatus.NOT_FOUND,
                ex.getMessage(),
                LocalDateTime.now(),
                request.getRequestURI(),
                "User does not exist"
                );
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(UserEmailNotFoundException.class)
    public ResponseEntity<ApiError> handleUserEmailNotFound(UserNotFoundException ex, HttpServletRequest request) {
        ApiError error = new ApiError(HttpStatus.NOT_FOUND,
                ex.getMessage(),
                LocalDateTime.now(),
                request.getRequestURI(),
                "Bad Credentials: User with this email does not exist"
        );
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(ProfileAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleProfileAlreadyExist(ProfileAlreadyExistsException ex, HttpServletRequest request) {
        ApiError error = new ApiError(HttpStatus.CONFLICT,
                ex.getMessage(),
                LocalDateTime.now(),
                request.getRequestURI(),
                "Profile already exist"
        );
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<ApiError> handleProfileNotFound(ProfileNotFoundException ex, HttpServletRequest request) {
        ApiError error = new ApiError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                LocalDateTime.now(),
                request.getRequestURI(),
                "Profile does not exist"
        );
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> handleEmailAlreadyExists(EmailAlreadyExistsException ex, HttpServletRequest request) {
        ApiError error = new ApiError(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                LocalDateTime.now(),
                request.getRequestURI(),
                "Email already exist"
        );
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex, HttpServletRequest request) {
        ApiError error = new ApiError(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                LocalDateTime.now(),
                request.getRequestURI(),
                "Username already exists"
        );
        return new ResponseEntity<>(error, error.getStatus());
    }
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotificationNotFound(NotificationNotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiError> handleInvalidTokenException(InvalidTokenException ex, HttpServletRequest request) {
        ApiError error = new ApiError(
                HttpStatus.UNAUTHORIZED,
                ex.getMessage(),
                LocalDateTime.now(),
                request.getRequestURI(),
                "Invalid or expired token"
        );
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        ApiError error = new ApiError(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password.",
                LocalDateTime.now(),
                request.getRequestURI(),
                "Authentication failed"
        );
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiError> handleUsernameNotFound(UsernameNotFoundException ex, HttpServletRequest request) {
        ApiError error = new ApiError(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password.",
                LocalDateTime.now(),
                request.getRequestURI(),
                "Authentication failed"
        );
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabledException(DisabledException ex, HttpServletRequest request) {
        ApiError error = new ApiError(
                HttpStatus.UNAUTHORIZED,
                "Account is disabled.",
                LocalDateTime.now(),
                request.getRequestURI(),
                "Authentication failed"
        );
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiError> handleLockedException(LockedException ex, HttpServletRequest request) {
        ApiError error = new ApiError(
                HttpStatus.UNAUTHORIZED,
                "Account is locked.",
                LocalDateTime.now(),
                request.getRequestURI(),
                "Authentication failed"
        );
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                LocalDateTime.now(),
                request.getRequestURI(),
                "Invalid request argument"
        );
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(StolenDeviceNotFoundException.class)
    public ResponseEntity<ApiError> handleStolenDeviceNotFound(StolenDeviceNotFoundException ex, HttpServletRequest request) {
        ApiError error = new ApiError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                LocalDateTime.now(),
                request.getRequestURI(),
                "Stolen device not found"
        );
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(DeviceAlreadyReportedException.class)
    public ResponseEntity<ApiError> handleDeviceAlreadyReported(DeviceAlreadyReportedException ex, HttpServletRequest request) {
        ApiError error = new ApiError(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                LocalDateTime.now(),
                request.getRequestURI(),
                "Device already reported as stolen"
        );
        return new ResponseEntity<>(error, error.getStatus());
    }

    @ExceptionHandler(InvalidCoordinatesException.class)
    public ResponseEntity<ApiError> handleInvalidCoordinates(InvalidCoordinatesException ex, HttpServletRequest request) {
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                LocalDateTime.now(),
                request.getRequestURI(),
                "Invalid coordinates provided"
        );
        return new ResponseEntity<>(error, error.getStatus());
    }
}
