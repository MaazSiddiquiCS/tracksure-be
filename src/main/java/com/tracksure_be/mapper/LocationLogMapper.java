package com.tracksure_be.mapper;

import com.tracksure_be.dto.LocationLogDTO;
import com.tracksure_be.entity.LocationLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationLogMapper {
    @Mapping(source = "subjectDevice.deviceId", target = "subjectDeviceId")
    @Mapping(target = "latitude", expression = "java(log.getLocation() != null ? log.getLocation().getY() : null)")
    @Mapping(target = "longitude", expression = "java(log.getLocation() != null ? log.getLocation().getX() : null)")
    LocationLogDTO toDto(LocationLog log);
}