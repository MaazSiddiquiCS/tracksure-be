package com.tracksure_be.controller;

import com.tracksure_be.dto.SafetyScoreDto;
import com.tracksure_be.dto.TheftReportDto;
import com.tracksure_be.security.UserPrincipal;
import com.tracksure_be.service.StolenDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for safety checks and police export functionality
 */
@RestController
@RequestMapping("/api/safety")
@RequiredArgsConstructor
@Tag(name = "Safety", description = "Endpoints for area safety assessment and police reporting")
public class SafetyController {

    private final StolenDeviceService stolenDeviceService;

    /**
     * Check safety score for a specific area
     * GET /api/safety/check-area?lat=x&lon=y
     */
    @GetMapping("/check-area")
    @Operation(summary = "Check area safety score", 
            description = "Assess the safety of an area based on stolen device reports within 5km radius")
    public ResponseEntity<SafetyScoreDto> checkAreaSafety(
            @RequestParam(name = "lat") Double latitude,
            @RequestParam(name = "lon") Double longitude
    ) {
        SafetyScoreDto safetyScore = stolenDeviceService.checkAreaSafety(latitude, longitude);
        return ResponseEntity.ok(safetyScore);
    }

    /**
     * Generate formal theft report for authorities
     * GET /api/safety/police-export/{deviceId}
     */
    @GetMapping("/police-export/{deviceId}")
    @Operation(summary = "Generate police theft report", 
            description = "Generate a formal theft report for authorities with all relevant device and incident details")
    public ResponseEntity<TheftReportDto> generatePoliceReport(
            @PathVariable Long deviceId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TheftReportDto report = stolenDeviceService.generatePoliceReport(
                deviceId,
                principal.getUserId()
        );
        return ResponseEntity.ok(report);
    }
}
