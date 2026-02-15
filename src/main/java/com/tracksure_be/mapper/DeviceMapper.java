package com.tracksure_be.mapper;

import com.tracksure_be.dto.DeviceDTO;
import com.tracksure_be.entity.Device;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeviceMapper {
    @Mapping(source = "ownerUser.userId", target = "ownerUserId")
    DeviceDTO toDto(Device device);
}