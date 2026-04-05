package com.tracksure_be.controller;

import com.tracksure_be.dto.LocationLogResponse;
import com.tracksure_be.service.LocationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}

