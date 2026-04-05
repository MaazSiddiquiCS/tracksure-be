package com.tracksure_be.mapper;

import com.tracksure_be.dto.LocationLogRequest;
import com.tracksure_be.dto.LocationLogResponse;
import com.tracksure_be.entity.LocationLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationLogMapper {

    @Mapping(source = "subjectDevice.deviceId", target = "subjectDeviceId")
    @Mapping(source = "uploaderDevice.deviceId", target = "uploaderDeviceId")
    @Mapping(target = "latitude", expression = "java(log.getLocation() != null ? log.getLocation().getY() : null)")
    @Mapping(target = "longitude", expression = "java(log.getLocation() != null ? log.getLocation().getX() : null)")
    LocationLogResponse toResponse(LocationLog log);

    @Mapping(target = "locationId", ignore = true)
    LocationLog toEntity(LocationLogRequest request);
}