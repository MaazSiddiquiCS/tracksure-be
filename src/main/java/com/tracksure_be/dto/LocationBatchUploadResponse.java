package com.tracksure_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationBatchUploadResponse {

	/** Number of points successfully persisted in this request. */
	private int inserted;

	/** Number of points skipped because they were already stored (idempotency). */
	private int duplicates;

	/** Total number of points received in the request. */
	private int totalReceived;
}
