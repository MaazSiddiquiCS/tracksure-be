package com.tracksure_be.service.impl;

import com.tracksure_be.dto.LocationResponse;
import com.tracksure_be.entity.Device;
import com.tracksure_be.entity.Location;
import com.tracksure_be.enums.PermissionType;
import com.tracksure_be.repository.DeviceRepository;
import com.tracksure_be.repository.DeviceLinkRepository;
import com.tracksure_be.repository.LocationRepository;
import com.tracksure_be.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceLinkRepository deviceLinkRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<LocationResponse> getByDeviceId(Long deviceId, Long requesterUserId) {
        requireRequester(requesterUserId);
        if (deviceId == null) {
            throw new IllegalArgumentException("deviceId is required.");
        }

        enforceDeviceAccess(deviceId, requesterUserId);
        return locationRepository.findBySubjectDevice_DeviceId(deviceId)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> getByUserId(Long requesterUserId) {
        requireRequester(requesterUserId);

        return locationRepository.findAllBySubjectDevice_OwnerUser_UserIdOrderByRecordedAtDesc(requesterUserId).stream()
                .map(this::toResponse)
                .toList();
    }

    private LocationResponse toResponse(Location location) {
        return new LocationResponse(
                location.getLocationId(),
                location.getRecordedAt(),
                location.getReceivedAt(),
                location.getAccuracy(),
                location.getSource(),
                location.getLocation() != null ? location.getLocation().getY() : null,
                location.getLocation() != null ? location.getLocation().getX() : null,
                location.getSubjectDevice() != null ? location.getSubjectDevice().getDeviceId() : null,
                location.getUploaderDevice() != null ? location.getUploaderDevice().getDeviceId() : null,
                location.getOwnerUser() != null ? location.getOwnerUser().getUserId() : null
        );
    }

    private void requireRequester(Long requesterUserId) {
        if (requesterUserId == null) {
            throw new IllegalArgumentException("Authenticated user id is required.");
        }
    }

    private void enforceDeviceAccess(Long deviceId, Long requesterUserId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found for id: " + deviceId));
        Long ownerUserId = device.getOwnerUser() != null ? device.getOwnerUser().getUserId() : null;
        if (ownerUserId != null && ownerUserId.equals(requesterUserId)) {
            return;
        }

        boolean linked = deviceLinkRepository
                .findByFollowerUser_UserIdAndTargetDevice_DeviceId(requesterUserId, deviceId)
                .map(link -> link.getPermissionType() == PermissionType.TRACK
                        || link.getPermissionType() == PermissionType.TRACK)
                .orElse(false);

        if (!linked) {
            throw new IllegalArgumentException("Access denied: device is not shared with authenticated user.");
        }
    }
}
