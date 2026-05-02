package com.tracksure_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for global analytics statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsStatsDto {

    private Long totalStolenReports;
    private Long totalRecovered;
    private Long activeDevices;
    private Double recoveryRate;
}
