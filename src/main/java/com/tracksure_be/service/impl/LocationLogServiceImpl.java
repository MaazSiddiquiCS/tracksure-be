package com.tracksure_be.service.impl;

import com.tracksure_be.dto.LocationLogResponse;
import com.tracksure_be.entity.Device;
import com.tracksure_be.mapper.LocationLogMapper;
import com.tracksure_be.repository.DeviceRepository;
import com.tracksure_be.repository.LocationLogRepository;
import com.tracksure_be.service.LocationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationLogServiceImpl implements LocationLogService {

	private final LocationLogRepository locationLogRepository;
	private final LocationLogMapper locationLogMapper;
	private final DeviceRepository deviceRepository;

	@Override
	@Transactional(readOnly = true)
	public List<LocationLogResponse> getAll() {
		return locationLogRepository.findAll().stream()
				.map(locationLogMapper::toResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<LocationLogResponse> getByDeviceId(Long deviceId, Long requesterUserId) {
		requireRequester(requesterUserId);
		if (deviceId == null) {
			throw new IllegalArgumentException("deviceId is required.");
		}
		enforceDeviceAccess(deviceId, requesterUserId);

		return locationLogRepository.findAllBySubjectDevice_DeviceIdOrderByRecordedAtDesc(deviceId).stream()
				.map(locationLogMapper::toResponse)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<LocationLogResponse> getByUserId(Long requesterUserId) {
		requireRequester(requesterUserId);

		return locationLogRepository.findAllBySubjectDevice_OwnerUser_UserIdOrderByRecordedAtDesc(requesterUserId).stream()
				.map(locationLogMapper::toResponse)
				.toList();
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
		if (ownerUserId == null || !ownerUserId.equals(requesterUserId)) {
			throw new IllegalArgumentException("Access denied: device is not owned by authenticated user.");
		}
	}
}

