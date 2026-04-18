package com.tracksure_be.service;

import com.tracksure_be.dto.LocationLogResponse;

import java.util.List;

public interface LocationLogService {
	List<LocationLogResponse> getAll();

	List<LocationLogResponse> getByDeviceId(Long deviceId, Long requesterUserId);

	List<LocationLogResponse> getByUserId(Long requesterUserId);
}

