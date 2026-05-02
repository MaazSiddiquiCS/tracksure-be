package com.tracksure_be.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for heatmap data - represents a concentration point of stolen devices
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeatmapPointDto {

    private Double latitude;
    private Double longitude;
    private Long frequency; // count of stolen devices at this location
    private Double intensity; // frequency-based intensity score (0-1)

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastReportedAt;
}
