package com.tracksure_be.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for reporting a stolen device
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportStolenDeviceRequest {

    @NotNull(message = "Device ID is required")
    private Long deviceId;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

}
