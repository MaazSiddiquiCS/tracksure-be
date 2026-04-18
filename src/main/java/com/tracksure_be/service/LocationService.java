package com.tracksure_be.service;

import com.tracksure_be.dto.LocationResponse;

import java.util.List;
import java.util.Optional;

public interface LocationService {
    Optional<LocationResponse> getByDeviceId(Long deviceId, Long requesterUserId);
    List<LocationResponse> getByUserId(Long requesterUserId);
}
