package com.tracksure_be.mapper;

import com.tracksure_be.dto.DeviceRequest;
import com.tracksure_be.dto.DeviceResponse;
import com.tracksure_be.entity.Device;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    @Mapping(source = "ownerUser.userId", target = "ownerUserId")
    DeviceResponse toResponse(Device device);

    @Mapping(target = "deviceId", ignore = true)
    @Mapping(target = "lastSeenAt", ignore = true)
    Device toEntity(DeviceRequest request);
}