package com.tracksure_be.mapper;

import com.tracksure_be.dto.UploadBatchRequest;
import com.tracksure_be.dto.UploadBatchResponse;
import com.tracksure_be.entity.UploadBatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UploadBatchMapper {

    @Mapping(source = "uploaderDevice.deviceId", target = "uploaderDeviceId")
    UploadBatchResponse toResponse(UploadBatch batch);

    @Mapping(target = "batchId", ignore = true)
    @Mapping(target = "uploadedAt", ignore = true)
    UploadBatch toEntity(UploadBatchRequest request);
}