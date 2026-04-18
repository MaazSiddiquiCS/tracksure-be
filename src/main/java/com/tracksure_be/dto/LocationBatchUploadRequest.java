package com.tracksure_be.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationBatchUploadRequest {

	/** Client-generated UUID reused on retries for batch-level idempotency. */
	@NotBlank(message = "clientBatchUuid must not be blank")
	private String clientBatchUuid;

	/** Mesh peer ID of the device being tracked; backend resolves this to subject device id. */
	@NotBlank(message = "subjectPeerId must not be blank")
	private String subjectPeerId;

	/** Optional uploader device id; if provided it must belong to the authenticated user, otherwise backend derives one. */
	private Long uploaderDeviceId;

	@NotNull(message = "points must not be null")
	@NotEmpty(message = "points must not be empty")
	@Size(max = 500, message = "A single batch may contain at most 500 points")
	private List<@Valid LocationPointDto> points;

	public LocationBatchUploadRequest(String subjectPeerId, Long uploaderDeviceId, List<@Valid LocationPointDto> points) {
		this.subjectPeerId = subjectPeerId;
		this.uploaderDeviceId = uploaderDeviceId;
		this.points = points;
	}
}
