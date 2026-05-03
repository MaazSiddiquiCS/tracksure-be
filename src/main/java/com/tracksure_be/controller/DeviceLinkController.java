package com.tracksure_be.controller;

import com.tracksure_be.dto.DeviceLinkCreateRequest;
import com.tracksure_be.dto.DeviceLinkPermissionUpdateRequest;
import com.tracksure_be.dto.TrackedDeviceLinkResponse;
import com.tracksure_be.dto.TrackerLinkResponse;
import com.tracksure_be.security.UserPrincipal;
import com.tracksure_be.service.DeviceLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/device-links")
@RequiredArgsConstructor
@Tag(name = "Device Links", description = "Device tracking links between users")
public class DeviceLinkController {

    private final DeviceLinkService deviceLinkService;

    @PostMapping
    @Operation(summary = "Create or update a device tracking link")
    public ResponseEntity<TrackedDeviceLinkResponse> createLink(
            @Valid @RequestBody DeviceLinkCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long authenticatedUserId = principal != null ? principal.getUserId() : null;
        TrackedDeviceLinkResponse response = deviceLinkService.createLink(request, authenticatedUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tracked")
    @Operation(summary = "List devices the authenticated user can track")
    public ResponseEntity<List<TrackedDeviceLinkResponse>> getTrackedDevices(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long authenticatedUserId = principal != null ? principal.getUserId() : null;
        return ResponseEntity.ok(deviceLinkService.getTrackedDevices(authenticatedUserId));
    }

    @GetMapping("/trackers")
    @Operation(summary = "List users who can track my devices")
    public ResponseEntity<List<TrackerLinkResponse>> getTrackers(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long authenticatedUserId = principal != null ? principal.getUserId() : null;
        return ResponseEntity.ok(deviceLinkService.getTrackers(authenticatedUserId));
    }

    @PatchMapping("/{linkId}")
    @Operation(summary = "Update device link permission")
    public ResponseEntity<TrackedDeviceLinkResponse> updatePermission(
            @PathVariable Long linkId,
            @Valid @RequestBody DeviceLinkPermissionUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long authenticatedUserId = principal != null ? principal.getUserId() : null;
        return ResponseEntity.ok(deviceLinkService.updatePermission(linkId, request, authenticatedUserId));
    }

    @DeleteMapping("/{linkId}")
    @Operation(summary = "Delete device link")
    public ResponseEntity<Void> deleteLink(
            @PathVariable Long linkId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long authenticatedUserId = principal != null ? principal.getUserId() : null;
        deviceLinkService.deleteLink(linkId, authenticatedUserId);
        return ResponseEntity.noContent().build();
    }
}
