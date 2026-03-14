package com.tracksure_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadBatchRequest {
    private String clientBatchUuid;
    private Integer pointsCount;
    private Long uploaderDeviceId;
}

