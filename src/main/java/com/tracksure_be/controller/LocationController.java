package com.tracksure_be.controller;

import com.tracksure_be.dto.LocationResponse;
import com.tracksure_be.security.UserPrincipal;
import com.tracksure_be.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/location")
@RequiredArgsConstructor
@Tag(name = "Locations", description = "Latest location projections per device")
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "Get latest location by device id")
    public ResponseEntity<LocationResponse> getByDeviceId(
            @PathVariable Long deviceId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long requesterUserId = principal != null ? principal.getUserId() : null;
        return locationService.getByDeviceId(deviceId, requesterUserId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/me")
    @Operation(summary = "Get latest locations for all devices owned by user")
    public ResponseEntity<List<LocationResponse>> getByUserId(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long requesterUserId = principal != null ? principal.getUserId() : null;
        return ResponseEntity.ok(locationService.getByUserId(requesterUserId));
    }
}
