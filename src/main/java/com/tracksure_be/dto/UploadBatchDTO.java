package com.tracksure_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadBatchDTO {
    private Long batchId;
    private String clientBatchUuid;
    private Integer pointsCount;
    private Instant uploadedAt;
    private Long uploaderDeviceId;
}