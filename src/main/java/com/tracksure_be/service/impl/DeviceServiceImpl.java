package com.tracksure_be.service.impl;

import com.tracksure_be.dto.DeviceLinkUpsertRequest;
import com.tracksure_be.dto.DeviceLinkUpsertResponse;
import com.tracksure_be.dto.DeviceResponse;
import com.tracksure_be.entity.Device;
import com.tracksure_be.entity.User;
import com.tracksure_be.enums.DeviceStatus;
import com.tracksure_be.repository.DeviceRepository;
import com.tracksure_be.repository.UserRepository;
import com.tracksure_be.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public DeviceLinkUpsertResponse link(DeviceLinkUpsertRequest request, Long authenticatedUserId) {
        if (authenticatedUserId == null) {
            throw new IllegalArgumentException("Authenticated principal is required.");
        }

        String normalizedPeerId = request.getPeerId().trim().toLowerCase();
        if (normalizedPeerId.isBlank()) {
            throw new IllegalArgumentException("peerId must not be blank");
        }
        if (normalizedPeerId.length() > 16) {
            throw new IllegalArgumentException("peerId length must be <= 16 characters");
        }

        User owner = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

        Device linked = deviceRepository.findByPeerId(normalizedPeerId)
                .map(existing -> {
                    Long ownerId = existing.getOwnerUser() != null ? existing.getOwnerUser().getUserId() : null;
                    if (ownerId == null || !ownerId.equals(authenticatedUserId)) {
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
        return new DeviceLinkUpsertResponse(saved.getDeviceId(), saved.getPeerId(), saved.getDeviceName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponse> getByAuthenticatedUser(Long authenticatedUserId) {
        if (authenticatedUserId == null) {
            throw new IllegalArgumentException("Authenticated principal is required.");
        }

        return deviceRepository.findAllByOwnerUser_UserId(authenticatedUserId).stream()
                .sorted(Comparator.comparing(Device::getDeviceId))
                .map(this::toDeviceResponse)
                .toList();
    }

    private DeviceResponse toDeviceResponse(Device device) {
        return new DeviceResponse(
                device.getDeviceId(),
                device.getPeerId(),
                device.getDeviceName(),
                device.getStatus(),
                device.getLastSeenAt(),
                device.getOwnerUser() != null ? device.getOwnerUser().getUserId() : null
        );
    }

    private String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
