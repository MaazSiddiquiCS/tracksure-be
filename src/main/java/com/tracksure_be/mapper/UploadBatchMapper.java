package com.tracksure_be.mapper;

import com.tracksure_be.dto.UploadBatchDTO;
import com.tracksure_be.entity.UploadBatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UploadBatchMapper {
    @Mapping(source = "uploaderDevice.deviceId", target = "uploaderDeviceId")
    UploadBatchDTO toDto(UploadBatch batch);
}