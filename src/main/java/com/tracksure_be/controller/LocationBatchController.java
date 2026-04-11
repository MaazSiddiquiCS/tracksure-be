package com.tracksure_be.controller;

import com.tracksure_be.dto.LocationBatchUploadRequest;
import com.tracksure_be.dto.LocationBatchUploadResponse;
import com.tracksure_be.security.UserPrincipal;
import com.tracksure_be.service.LocationBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "Location Batch", description = "Batch location-point ingestion for offline/Wi-Fi-deferred uploads")
public class LocationBatchController {

	private final LocationBatchService locationBatchService;

	/**
	 * Accepts a batch of location points from a mobile device and persists them.
	 * Idempotent: re-posting the same {@code clientPointId} values will not create
	 * duplicates; the response counts both inserted and skipped points.
	 *
	 * <p>URL uses Google API-style custom-method suffix: {@code POST /v1/locations:batch}
	 */
	@PostMapping("/locations:batch")
	@Operation(
			summary = "Upload a batch of location points",
			description = "Persists up to 500 location points per call. " +
					"Duplicate points (identified by clientPointId) are silently skipped."
	)
	public ResponseEntity<LocationBatchUploadResponse> uploadBatch(
			@Valid @RequestBody LocationBatchUploadRequest request,
			@AuthenticationPrincipal UserPrincipal principal) {
		if (principal == null || principal.getUserId() == null) {
			throw new IllegalArgumentException("Authenticated principal is required.");
		}
		return ResponseEntity.ok(locationBatchService.uploadBatch(request, principal.getUserId()));
	}
}
