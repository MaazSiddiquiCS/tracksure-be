package com.tracksure_be.mapper;

import com.tracksure_be.dto.DeviceLinkDTO;
import com.tracksure_be.entity.DeviceLink;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeviceLinkMapper {
    @Mapping(source = "followerUser.userId", target = "followerId")
    @Mapping(source = "targetDevice.deviceId", target = "targetDeviceId")
    DeviceLinkDTO toDto(DeviceLink link);
}