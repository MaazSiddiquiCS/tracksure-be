package com.tracksure_be.service;

import com.tracksure_be.dto.DeviceLinkUpsertRequest;
import com.tracksure_be.dto.DeviceLinkUpsertResponse;
import com.tracksure_be.dto.DeviceResponse;

import java.util.List;

public interface DeviceService {
    DeviceLinkUpsertResponse link(DeviceLinkUpsertRequest request, Long authenticatedUserId);

    List<DeviceResponse> getByAuthenticatedUser(Long authenticatedUserId);
}
