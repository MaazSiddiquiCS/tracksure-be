package com.tracksure_be.controller;

import com.tracksure_be.dto.ProfileRequest;
import com.tracksure_be.dto.ProfileResponse;
import com.tracksure_be.security.UserPrincipal;
import com.tracksure_be.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Profile CRUD for authenticated users")
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    @Operation(summary = "Create profile for authenticated user")
    public ResponseEntity<ProfileResponse> createMine(
            @Valid @RequestBody ProfileRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long authenticatedUserId = principal != null ? principal.getUserId() : null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(profileService.createForAuthenticatedUser(request, authenticatedUserId));
    }

    @GetMapping("/me")
    @Operation(summary = "Get authenticated user's profile")
    public ResponseEntity<ProfileResponse> getMine(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long authenticatedUserId = principal != null ? principal.getUserId() : null;
        return ResponseEntity.ok(profileService.getMine(authenticatedUserId));
    }

    @PutMapping("/me")
    @Operation(summary = "Update authenticated user's profile")
    public ResponseEntity<ProfileResponse> updateMine(
            @Valid @RequestBody ProfileRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long authenticatedUserId = principal != null ? principal.getUserId() : null;
        return ResponseEntity.ok(profileService.updateMine(request, authenticatedUserId));
    }

    @DeleteMapping("/me")
    @Operation(summary = "Delete authenticated user's profile")
    public ResponseEntity<Void> deleteMine(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long authenticatedUserId = principal != null ? principal.getUserId() : null;
        profileService.deleteMine(authenticatedUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{profileId}")
    @Operation(summary = "Get profile by profile id")
    public ResponseEntity<ProfileResponse> getByProfileId(@PathVariable Long profileId) {
        return ResponseEntity.ok(profileService.getByProfileId(profileId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get profile by user id")
    public ResponseEntity<ProfileResponse> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getByUserId(userId));
    }
}
