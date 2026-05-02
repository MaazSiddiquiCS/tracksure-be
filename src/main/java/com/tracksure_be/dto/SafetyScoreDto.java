package com.tracksure_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for area safety check result
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SafetyScoreDto {

    private Double latitude;
    private Double longitude;
    private Integer safetyScore; // 0-100, where 100 is safest
    private String safetyLevel; // "Safe", "Caution", "Danger"
    private Long devicesInArea;
    private Double radiusKm;
    private String recommendation;
}
