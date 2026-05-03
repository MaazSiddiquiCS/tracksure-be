package com.tracksure_be.service;

import com.tracksure_be.dto.DeviceBasicResponse;
import com.tracksure_be.dto.DeviceLinkCreateRequest;
import com.tracksure_be.dto.DeviceLinkPermissionUpdateRequest;
import com.tracksure_be.dto.TrackedDeviceLinkResponse;
import com.tracksure_be.dto.TrackerLinkResponse;
import com.tracksure_be.dto.UserBasicResponse;
import com.tracksure_be.entity.Device;
import com.tracksure_be.entity.DeviceLink;
import com.tracksure_be.entity.User;
import com.tracksure_be.enums.PermissionType;
import com.tracksure_be.repository.DeviceLinkRepository;
import com.tracksure_be.repository.DeviceRepository;
import com.tracksure_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceLinkService {

    private final DeviceLinkRepository deviceLinkRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    @Transactional
    public TrackedDeviceLinkResponse createLink(DeviceLinkCreateRequest request, Long authenticatedUserId) {
        if (authenticatedUserId == null) {
            throw new IllegalArgumentException("Authenticated principal is required.");
        }
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null");
        }

        Long targetId = request.getTargetDeviceId();
        
        // Resolve peerId to Internal ID if targetDeviceId is null
        if (targetId == null && request.getPeerId() != null) {
            targetId = deviceRepository.findByPeerId(request.getPeerId())
                    .map(Device::getDeviceId)
                    .orElseThrow(() -> new IllegalArgumentException("Device with this Peer ID not found."));
        } else if (targetId == null) {
            throw new IllegalArgumentException("Either targetDeviceId or peerId must be provided.");
        }

        PermissionType permissionType = request.getPermissionType() != null
                ? request.getPermissionType()
                : PermissionType.TRACK;

        User follower = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

        Device targetDevice = deviceRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Target device not found."));

        if (targetDevice.getOwnerUser() != null
                && authenticatedUserId.equals(targetDevice.getOwnerUser().getUserId())) {
            throw new IllegalArgumentException("Cannot link to your own device.");
        }

        DeviceLink link = deviceLinkRepository
                .findByFollowerUser_UserIdAndTargetDevice_DeviceId(authenticatedUserId, targetDevice.getDeviceId())
                .map(existing -> {
                    existing.setPermissionType(permissionType);
                    return existing;
                })
                .orElseGet(() -> {
                    DeviceLink created = new DeviceLink();
                    created.setFollowerUser(follower);
                    created.setTargetDevice(targetDevice);
                    created.setPermissionType(permissionType);
                    return created;
                });

        DeviceLink saved = deviceLinkRepository.save(link);
        return toTrackedResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TrackedDeviceLinkResponse> getTrackedDevices(Long authenticatedUserId) {
        if (authenticatedUserId == null) {
            throw new IllegalArgumentException("Authenticated principal is required.");
        }

        return deviceLinkRepository.findAllByFollowerUser_UserId(authenticatedUserId).stream()
                .sorted(Comparator.comparing(DeviceLink::getLinkId))
                .map(this::toTrackedResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrackerLinkResponse> getTrackers(Long authenticatedUserId) {
        if (authenticatedUserId == null) {
            throw new IllegalArgumentException("Authenticated principal is required.");
        }

        return deviceLinkRepository.findAllByTargetDevice_OwnerUser_UserId(authenticatedUserId).stream()
                .sorted(Comparator.comparing(DeviceLink::getLinkId))
                .map(this::toTrackerResponse)
                .toList();
    }

    @Transactional
    public TrackedDeviceLinkResponse updatePermission(
            Long linkId,
            DeviceLinkPermissionUpdateRequest request,
            Long authenticatedUserId
    ) {
        if (authenticatedUserId == null) {
            throw new IllegalArgumentException("Authenticated principal is required.");
        }
        if (request == null || request.getPermissionType() == null) {
            throw new IllegalArgumentException("permissionType must not be null");
        }

        DeviceLink link = deviceLinkRepository.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Device link not found."));

        if (link.getFollowerUser() == null
                || !authenticatedUserId.equals(link.getFollowerUser().getUserId())) {
            throw new IllegalArgumentException("Unauthorized to update this device link.");
        }

        link.setPermissionType(request.getPermissionType());
        DeviceLink saved = deviceLinkRepository.save(link);
        return toTrackedResponse(saved);
    }

    @Transactional
    public void deleteLink(Long linkId, Long authenticatedUserId) {
        if (authenticatedUserId == null) {
            throw new IllegalArgumentException("Authenticated principal is required.");
        }

        DeviceLink link = deviceLinkRepository.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Device link not found."));

        if (link.getFollowerUser() == null
                || !authenticatedUserId.equals(link.getFollowerUser().getUserId())) {
            throw new IllegalArgumentException("Unauthorized to delete this device link.");
        }

        deviceLinkRepository.delete(link);
    }

    private TrackedDeviceLinkResponse toTrackedResponse(DeviceLink link) {
        return new TrackedDeviceLinkResponse(
                link.getLinkId(),
                link.getPermissionType(),
                toDeviceBasic(link.getTargetDevice()),
                toUserBasic(link.getTargetDevice() != null ? link.getTargetDevice().getOwnerUser() : null)
        );
    }

    private TrackerLinkResponse toTrackerResponse(DeviceLink link) {
        return new TrackerLinkResponse(
                link.getLinkId(),
                link.getPermissionType(),
                toUserBasic(link.getFollowerUser()),
                toDeviceBasic(link.getTargetDevice())
        );
    }

    private DeviceBasicResponse toDeviceBasic(Device device) {
        if (device == null) {
            return null;
        }
        return new DeviceBasicResponse(
                device.getDeviceId(),
                device.getPeerId(),
                device.getDeviceName(),
                device.getStatus(),
                device.getLastSeenAt()
        );
    }

    private UserBasicResponse toUserBasic(User user) {
        if (user == null) {
            return null;
        }
        return new UserBasicResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}
