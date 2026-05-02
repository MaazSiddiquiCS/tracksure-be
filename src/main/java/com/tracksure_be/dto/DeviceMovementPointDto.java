package com.tracksure_be.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for device movement coordinate (single point in time)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceMovementPointDto {

    private Double latitude;
    private Double longitude;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String formattedAddress;
    private String city;
}
