package com.tracksure_be.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(Long profileId) {
        super("Profile with id " + profileId + " not found");
    }

    public static ProfileNotFoundException forUserId(Long userId) {
        return new ProfileNotFoundException("Profile for user id " + userId + " not found");
    }

    public ProfileNotFoundException(String message) {
        super(message);
    }
}
