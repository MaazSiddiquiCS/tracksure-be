package com.tracksure_be.service;

import com.tracksure_be.dto.LocationBatchUploadRequest;
import com.tracksure_be.dto.LocationBatchUploadResponse;

public interface LocationBatchService {

	/**
	 * Persists a batch of location points, skipping any that have already been stored
	 * (idempotency via {@code client_point_id}).
	 *
	 * @param request batch upload request containing subject/uploader device IDs and points
	 * @return summary with inserted, duplicate, and total-received counts
	 */
	LocationBatchUploadResponse uploadBatch(LocationBatchUploadRequest request);
}
