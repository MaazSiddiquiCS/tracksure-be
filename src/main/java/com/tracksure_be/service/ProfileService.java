package com.tracksure_be.service;

import com.tracksure_be.dto.ProfileRequest;
import com.tracksure_be.dto.ProfileResponse;

public interface ProfileService {
    ProfileResponse createForAuthenticatedUser(ProfileRequest request, Long authenticatedUserId);

    ProfileResponse getMine(Long authenticatedUserId);

    ProfileResponse updateMine(ProfileRequest request, Long authenticatedUserId);

    void deleteMine(Long authenticatedUserId);

    ProfileResponse getByProfileId(Long profileId);

    ProfileResponse getByUserId(Long userId);
}
