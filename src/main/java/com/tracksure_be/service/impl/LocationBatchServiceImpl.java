package com.tracksure_be.service.impl;

import com.tracksure_be.dto.LocationBatchUploadRequest;
import com.tracksure_be.dto.LocationBatchUploadResponse;
import com.tracksure_be.dto.LocationPointDto;
import com.tracksure_be.entity.Device;
import com.tracksure_be.entity.LocationLog;
import com.tracksure_be.repository.DeviceRepository;
import com.tracksure_be.repository.LocationLogRepository;
import com.tracksure_be.service.LocationBatchService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class LocationBatchServiceImpl implements LocationBatchService {

	private static final GeometryFactory GEO_FACTORY =
			new GeometryFactory(new PrecisionModel(), 4326);

	private final LocationLogRepository locationLogRepository;
	private final DeviceRepository deviceRepository;

	@Override
	@Transactional
	public LocationBatchUploadResponse uploadBatch(LocationBatchUploadRequest request) {
		Device subjectDevice = deviceRepository.findById(request.getSubjectDeviceId())
				.orElseThrow(() -> new IllegalArgumentException(
						"Subject device not found: " + request.getSubjectDeviceId()));

		Device uploaderDevice = deviceRepository.findById(request.getUploaderDeviceId())
				.orElseThrow(() -> new IllegalArgumentException(
						"Uploader device not found: " + request.getUploaderDeviceId()));

		// Pre-fetch all known client_point_ids for this subject/uploader pair to
		// avoid per-row DB lookups.
		Set<String> existingIds = locationLogRepository.findClientPointIdsBySubjectAndUploader(
				request.getSubjectDeviceId(), request.getUploaderDeviceId());

		int totalReceived = request.getPoints().size();
		int duplicates = 0;

		Instant receivedAt = Instant.now();
		List<LocationLog> toInsert = new ArrayList<>(totalReceived);

		// Track IDs seen within this batch to handle intra-batch duplicates.
		Set<String> seenInBatch = new HashSet<>();

		for (LocationPointDto point : request.getPoints()) {
			String cpId = point.getClientPointId();

			if (existingIds.contains(cpId) || !seenInBatch.add(cpId)) {
				duplicates++;
				continue;
			}

			LocationLog log = new LocationLog();
			log.setRecordedAt(point.getRecordedAt());
			log.setReceivedAt(receivedAt);
			log.setAccuracy(point.getAccuracy());
			log.setSource(point.getSource());
			log.setClientPointId(cpId);
			log.setLocation(buildPoint(point.getLon(), point.getLat()));
			log.setSubjectDevice(subjectDevice);
			log.setUploaderDevice(uploaderDevice);

			toInsert.add(log);
		}

		locationLogRepository.saveAll(toInsert);

		int inserted = toInsert.size();
		return new LocationBatchUploadResponse(inserted, duplicates, totalReceived);
	}

	private Point buildPoint(double lon, double lat) {
		return GEO_FACTORY.createPoint(new Coordinate(lon, lat));
	}
}
