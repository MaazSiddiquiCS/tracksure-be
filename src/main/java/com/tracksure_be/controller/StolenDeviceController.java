package com.tracksure_be.controller;

import com.tracksure_be.dto.ReportStolenDeviceRequest;
import com.tracksure_be.dto.StolenDeviceResponse;
import com.tracksure_be.security.UserPrincipal;
import com.tracksure_be.service.StolenDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

/**
 * Controller for stolen device reporting and management
 */
@RestController
@RequestMapping("/api/stolen")
@RequiredArgsConstructor
@Tag(name = "Stolen Devices", description = "Endpoints for reporting and managing stolen/lost devices")
public class StolenDeviceController {

    private final StolenDeviceService stolenDeviceService;

    /**
     * Report a stolen/lost device with location
     * POST /api/stolen/report
     */
    @PostMapping("/report")
    @Operation(summary = "Report a stolen or lost device", 
            description = "Report a stolen device with coordinates. Geocoding is performed automatically.")
    public ResponseEntity<StolenDeviceResponse> reportStolenDevice(
            @Valid @RequestBody ReportStolenDeviceRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        StolenDeviceResponse response = stolenDeviceService.reportStolenDevice(
                request, 
                principal.getUserId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all stolen devices for the authenticated user with pagination
     * GET /api/stolen/my-devices
     */
    @GetMapping("/my-devices")
    @Operation(summary = "Get user's stolen devices", 
            description = "Retrieve paginated list of stolen devices reported by the authenticated user")
    public ResponseEntity<Page<StolenDeviceResponse>> getMyDevices(
            Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Page<StolenDeviceResponse> devices = stolenDeviceService.getMyDevices(
                principal.getUserId(), 
                pageable
        );
        return ResponseEntity.ok(devices);
    }

    /**
     * Get details of a specific stolen device
     * GET /api/stolen/details/{deviceId}
     */
    @GetMapping("/details/{deviceId}")
    @Operation(summary = "Get stolen device details", 
            description = "Retrieve detailed information about a specific stolen device")
    public ResponseEntity<StolenDeviceResponse> getDeviceDetails(
            @PathVariable Long deviceId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        StolenDeviceResponse device = stolenDeviceService.getDeviceDetails(
                deviceId, 
                principal.getUserId()
        );
        return ResponseEntity.ok(device);
    }

    /**
     * Mark a device as recovered (soft delete - sets isRecovered flag)
     * PATCH /api/stolen/recover/{deviceId}
     */
    @PatchMapping("/recover/{deviceId}")
    @Operation(summary = "Mark device as recovered",
            description = "Mark a stolen device as recovered. Device data is preserved for analytics.")
    public ResponseEntity<StolenDeviceResponse> recoverDevice(
            @PathVariable Long deviceId,
            @AuthenticationPrincipal UserPrincipal principal
    ) throws AccessDeniedException {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        StolenDeviceResponse device = stolenDeviceService.recoverDevice(
                deviceId,
                principal.getUserId()
        );
        return ResponseEntity.ok(device);
    }
}
