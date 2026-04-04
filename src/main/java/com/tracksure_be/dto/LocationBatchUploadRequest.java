package com.tracksure_be.dto;

import jakarta.validation.Valid;
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

	/** Database ID of the device being tracked. */
	@NotNull(message = "subjectDeviceId must not be null")
	private Long subjectDeviceId;

	/** Database ID of the device performing the upload (may differ from subject). */
	@NotNull(message = "uploaderDeviceId must not be null")
	private Long uploaderDeviceId;

	@NotNull(message = "points must not be null")
	@NotEmpty(message = "points must not be empty")
	@Size(max = 500, message = "A single batch may contain at most 500 points")
	private List<@Valid LocationPointDto> points;
}
