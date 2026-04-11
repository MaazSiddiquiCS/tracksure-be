package com.tracksure_be.controller;

import com.tracksure_be.dto.LocationLogResponse;
import com.tracksure_be.security.UserPrincipal;
import com.tracksure_be.service.LocationLogService;
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
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "Location Logs", description = "Dev/test endpoints for inspecting persisted location logs")
public class LocationLogController {

	private final LocationLogService locationLogService;

	@GetMapping("/location-logs")
	@Operation(summary = "Get all location logs (test/dev)")
	public ResponseEntity<List<LocationLogResponse>> getAll() {
		return ResponseEntity.ok(locationLogService.getAll());
	}

	@GetMapping("/location-logs/device/{deviceId}")
	@Operation(summary = "Get location logs for a single subject device")
	public ResponseEntity<List<LocationLogResponse>> getByDeviceId(
			@PathVariable Long deviceId,
			@AuthenticationPrincipal UserPrincipal principal
	) {
		Long requesterUserId = principal != null ? principal.getUserId() : null;
		return ResponseEntity.ok(locationLogService.getByDeviceId(deviceId, requesterUserId));
	}

	@GetMapping("/location-logs/user/{userId}")
	@Operation(summary = "Get location logs for all devices owned by a user")
	public ResponseEntity<List<LocationLogResponse>> getByUserId(
			@PathVariable Long userId,
			@AuthenticationPrincipal UserPrincipal principal
	) {
		Long requesterUserId = principal != null ? principal.getUserId() : null;
		return ResponseEntity.ok(locationLogService.getByUserId(userId, requesterUserId));
	}
}

