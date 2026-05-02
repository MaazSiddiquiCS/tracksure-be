package com.tracksure_be.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for danger zones - cities/neighborhoods ranked by stolen device count
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DangerZoneDto {

    private String city;
    private Long deviceCount;
    private Integer riskLevel; // 1-10 scale based on device count

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastReportedAt;

    private String riskDescription; // e.g., "High Risk", "Medium Risk"
}
