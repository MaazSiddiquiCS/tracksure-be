package com.tracksure_be.controller;

import com.tracksure_be.dto.AnalyticsStatsDto;
import com.tracksure_be.dto.DangerZoneDto;
import com.tracksure_be.dto.DeviceMovementPointDto;
import com.tracksure_be.dto.HeatmapPointDto;
import com.tracksure_be.security.UserPrincipal;
import com.tracksure_be.service.StolenDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for analytics and heatmap data endpoints
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Endpoints for theft analytics, heatmaps, and danger zone identification")
public class AnalyticsController {

    private final StolenDeviceService stolenDeviceService;

    /**
     * Get heatmap data - concentration points of stolen devices
     * GET /api/analytics/heatmap
     */
    @GetMapping("/heatmap")
    @Operation(summary = "Get heatmap data", 
            description = "Retrieve coordinates and intensity scores for stolen device concentration points")
    public ResponseEntity<List<HeatmapPointDto>> getHeatmapData() {
        List<HeatmapPointDto> heatmapData = stolenDeviceService.getHeatmapData();
        return ResponseEntity.ok(heatmapData);
    }

    /**
     * Get danger zones - cities ranked by stolen device count
     * GET /api/analytics/danger-zones
     */
    @GetMapping("/danger-zones")
    @Operation(summary = "Get danger zones", 
            description = "Retrieve paginated list of cities/neighborhoods ranked by stolen device count with risk levels")
    public ResponseEntity<Page<DangerZoneDto>> getDangerZones(Pageable pageable) {
        Page<DangerZoneDto> dangerZones = stolenDeviceService.getDangerZones(pageable);
        return ResponseEntity.ok(dangerZones);
    }

    /**
     * Get movement path for a specific device over the last 24 hours
     * GET /api/analytics/path/{deviceId}
     */
    @GetMapping("/path/{deviceId}")
    @Operation(summary = "Get device movement path", 
            description = "Retrieve coordinates showing the movement of a specific device over the last 24 hours")
    public ResponseEntity<List<DeviceMovementPointDto>> getDeviceMovementPath(
            @PathVariable Long deviceId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<DeviceMovementPointDto> path = stolenDeviceService.getDeviceMovementPath(
                deviceId,
                principal.getUserId()
        );
        return ResponseEntity.ok(path);
    }

    /**
     * Get global analytics statistics
     * GET /api/analytics/stats
     */
    @GetMapping("/stats")
    @Operation(summary = "Get global analytics statistics", 
            description = "Retrieve global statistics: total stolen reports, recovered devices, active nodes, and recovery rate")
    public ResponseEntity<AnalyticsStatsDto> getAnalyticsStats() {
        AnalyticsStatsDto stats = stolenDeviceService.getAnalyticsStats();
        return ResponseEntity.ok(stats);
    }
}
