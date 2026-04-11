package com.tracksure_be.controller;

import com.tracksure_be.dto.DeviceLinkUpsertRequest;
import com.tracksure_be.dto.DeviceLinkUpsertResponse;
import com.tracksure_be.entity.Device;
import com.tracksure_be.entity.User;
import com.tracksure_be.enums.DeviceStatus;
import com.tracksure_be.repository.DeviceRepository;
import com.tracksure_be.repository.UserRepository;
import com.tracksure_be.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
@Tag(name = "Device", description = "Authenticated device registration/linking")
public class DeviceController {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    @PostMapping("/link")
    @Transactional
    @Operation(summary = "Link current app/device peer id to authenticated account")
    public ResponseEntity<DeviceLinkUpsertResponse> link(
            @Valid @RequestBody DeviceLinkUpsertRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null || principal.getUserId() == null) {
            throw new IllegalArgumentException("Authenticated principal is required.");
        }

        String normalizedPeerId = request.getPeerId().trim().toLowerCase();
        if (normalizedPeerId.isBlank()) {
            throw new IllegalArgumentException("peerId must not be blank");
        }
        if (normalizedPeerId.length() > 16) {
            throw new IllegalArgumentException("peerId length must be <= 16 characters");
        }

        User owner = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

        Device linked = deviceRepository.findByPeerId(normalizedPeerId)
                .map(existing -> {
                    Long ownerId = existing.getOwnerUser() != null ? existing.getOwnerUser().getUserId() : null;
                    if (ownerId == null || !ownerId.equals(principal.getUserId())) {
                        throw new IllegalArgumentException("peerId is already linked to a different user.");
                    }
                    String incomingName = normalizeName(request.getDeviceName());
                    if (incomingName != null) {
                        existing.setDeviceName(incomingName);
                    }
                    existing.setStatus(DeviceStatus.ACTIVE);
                    existing.setLastSeenAt(Instant.now());
                    return existing;
                })
                .orElseGet(() -> {
                    Device created = new Device();
                    created.setPeerId(normalizedPeerId);
                    created.setDeviceName(normalizeName(request.getDeviceName()));
                    created.setStatus(DeviceStatus.ACTIVE);
                    created.setLastSeenAt(Instant.now());
                    created.setOwnerUser(owner);
                    return created;
                });

        Device saved = deviceRepository.save(linked);
        return ResponseEntity.ok(new DeviceLinkUpsertResponse(
                saved.getDeviceId(),
                saved.getPeerId(),
                saved.getDeviceName()
        ));
    }

    private String normalizeName(String name) {
        if (name == null) return null;
        String trimmed = name.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
