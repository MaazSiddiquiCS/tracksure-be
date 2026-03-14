package com.tracksure_be.mapper;

import com.tracksure_be.dto.DeviceLinkRequest;
import com.tracksure_be.dto.DeviceLinkResponse;
import com.tracksure_be.entity.DeviceLink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeviceLinkMapper {

    @Mapping(source = "followerUser.userId", target = "followerId")
    @Mapping(source = "targetDevice.deviceId", target = "targetDeviceId")
    DeviceLinkResponse toResponse(DeviceLink link);

    @Mapping(target = "linkId", ignore = true)
    DeviceLink toEntity(DeviceLinkRequest request);
}