package com.tracksure_be.service.impl;

import com.tracksure_be.dto.ProfileRequest;
import com.tracksure_be.dto.ProfileResponse;
import com.tracksure_be.entity.Profile;
import com.tracksure_be.entity.User;
import com.tracksure_be.exception.ProfileAlreadyExistsException;
import com.tracksure_be.exception.ProfileNotFoundException;
import com.tracksure_be.exception.UserNotFoundException;
import com.tracksure_be.mapper.ProfileMapper;
import com.tracksure_be.repository.ProfileRepository;
import com.tracksure_be.repository.UserRepository;
import com.tracksure_be.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;

    @Override
    @Transactional
    public ProfileResponse createForAuthenticatedUser(ProfileRequest request, Long authenticatedUserId) {
        requireAuthenticated(authenticatedUserId);

        if (profileRepository.existsByUser_UserId(authenticatedUserId)) {
            throw new ProfileAlreadyExistsException(authenticatedUserId);
        }

        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException(authenticatedUserId));

        validatePhoneNumber(request.getPhoneNumber(), authenticatedUserId);

        Profile profile = profileMapper.toEntity(request);
        profile.setFullName(normalize(request.getFullName()));
        profile.setPhoneNumber(normalize(request.getPhoneNumber()));
        profile.setBio(normalize(request.getBio()));
        profile.setProfilePic(normalize(request.getProfilePic()));
        profile.setUser(user);

        return profileMapper.toResponse(profileRepository.save(profile));
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getMine(Long authenticatedUserId) {
        requireAuthenticated(authenticatedUserId);
        Profile profile = profileRepository.findByUser_UserId(authenticatedUserId)
                .orElseThrow(() -> ProfileNotFoundException.forUserId(authenticatedUserId));
        return profileMapper.toResponse(profile);
    }

    @Override
    @Transactional
    public ProfileResponse updateMine(ProfileRequest request, Long authenticatedUserId) {
        requireAuthenticated(authenticatedUserId);
        Profile profile = profileRepository.findByUser_UserId(authenticatedUserId)
                .orElseThrow(() -> ProfileNotFoundException.forUserId(authenticatedUserId));

        validatePhoneNumber(request.getPhoneNumber(), authenticatedUserId);

        profile.setFullName(normalize(request.getFullName()));
        profile.setPhoneNumber(normalize(request.getPhoneNumber()));
        profile.setBio(normalize(request.getBio()));
        profile.setProfilePic(normalize(request.getProfilePic()));

        return profileMapper.toResponse(profileRepository.save(profile));
    }

    @Override
    @Transactional
    public void deleteMine(Long authenticatedUserId) {
        requireAuthenticated(authenticatedUserId);
        Profile profile = profileRepository.findByUser_UserId(authenticatedUserId)
                .orElseThrow(() -> ProfileNotFoundException.forUserId(authenticatedUserId));
        profileRepository.delete(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getByProfileId(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ProfileNotFoundException(profileId));
        return profileMapper.toResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getByUserId(Long userId) {
        Profile profile = profileRepository.findByUser_UserId(userId)
                .orElseThrow(() -> ProfileNotFoundException.forUserId(userId));
        return profileMapper.toResponse(profile);
    }

    private void requireAuthenticated(Long authenticatedUserId) {
        if (authenticatedUserId == null) {
            throw new IllegalArgumentException("Authenticated principal is required.");
        }
    }

    private void validatePhoneNumber(String phoneNumber, Long currentUserId) {
        String normalized = normalize(phoneNumber);
        if (normalized == null) {
            return;
        }
        if (profileRepository.existsByPhoneNumberAndUser_UserIdNot(normalized, currentUserId)) {
            throw new IllegalArgumentException("phoneNumber is already used by another profile");
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
