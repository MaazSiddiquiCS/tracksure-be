package com.tracksure_be.dto;

import com.tracksure_be.enums.LocationSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationResponse {
    private Long locationId;
    private Instant recordedAt;
    private Instant receivedAt;
    private Double accuracy;
    private LocationSource source;
    private Double latitude;
    private Double longitude;
    private Long subjectDeviceId;
    private Long uploaderDeviceId;
    private Long ownerUserId;
}
