package com.tracksure_be.service.impl;

import com.tracksure_be.dto.LocationBatchUploadRequest;
import com.tracksure_be.dto.LocationBatchUploadResponse;
import com.tracksure_be.dto.LocationPointDto;
import com.tracksure_be.entity.Device;
import com.tracksure_be.entity.Location;
import com.tracksure_be.entity.LocationLog;
import com.tracksure_be.entity.UploadBatch;
import com.tracksure_be.enums.LocationSource;
import com.tracksure_be.repository.DeviceRepository;
import com.tracksure_be.repository.LocationRepository;
import com.tracksure_be.repository.LocationLogRepository;
import com.tracksure_be.repository.UploadBatchRepository;
import com.tracksure_be.service.LocationBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationBatchServiceImpl implements LocationBatchService {

	private static final GeometryFactory GEO_FACTORY =
			new GeometryFactory(new PrecisionModel(), 4326);

	private final LocationLogRepository locationLogRepository;
	private final LocationRepository locationRepository;
	private final DeviceRepository deviceRepository;
	private final UploadBatchRepository uploadBatchRepository;

	@Override
	@Transactional
	public LocationBatchUploadResponse uploadBatch(LocationBatchUploadRequest request, Long authenticatedUserId) {
		Device uploaderDevice = resolveAuthenticatedUploader(authenticatedUserId, request.getUploaderDeviceId());
		validateClientBatchUuid(request.getClientBatchUuid());
        String normalizedSubjectPeerId = normalizeSubjectPeerId(request.getSubjectPeerId());
		int totalReceived = request.getPoints() != null ? request.getPoints().size() : 0;
		Instant receivedAt = Instant.now();

		if (uploadBatchRepository.existsByUploaderDevice_DeviceIdAndClientBatchUuid(
				uploaderDevice.getDeviceId(), request.getClientBatchUuid())) {
			return new LocationBatchUploadResponse(0, totalReceived, totalReceived);
		}

		Device subjectDevice = deviceRepository.findByPeerId(normalizedSubjectPeerId).orElse(null);
		if (subjectDevice == null) {
			log.warn("Skipping batch with unresolved subject peerId={} uploaderDeviceId={} points={}",
					normalizedSubjectPeerId,
					uploaderDevice.getDeviceId(),
					totalReceived);
			saveUploadBatchSafely(request.getClientBatchUuid(), totalReceived, receivedAt, uploaderDevice);
			return new LocationBatchUploadResponse(0, totalReceived, totalReceived);
		}

		// Pre-fetch all known client_point_ids for this subject/uploader pair to
		// avoid per-row DB lookups.
		Set<String> existingIds = locationLogRepository.findClientPointIdsBySubjectAndUploader(
				subjectDevice.getDeviceId(), uploaderDevice.getDeviceId());

		Set<String> requestedDedupKeys = request.getPoints().stream()
				.map(point -> buildDedupKey(
						subjectDevice.getDeviceId(),
						point,
						normalizeSource(point.getSource())
				))
				.collect(Collectors.toSet());
		Set<String> existingDedupKeys = requestedDedupKeys.isEmpty()
				? Set.of()
				: locationLogRepository.findDedupKeysBySubjectAndDedupKeyIn(subjectDevice.getDeviceId(), requestedDedupKeys);

		int duplicates = 0;
		List<LocationLog> toInsert = new ArrayList<>(totalReceived);

		// Track IDs seen within this batch to handle intra-batch duplicates.
		Set<String> seenInBatch = new HashSet<>();
		Set<String> seenDedupInBatch = new HashSet<>();

		for (LocationPointDto point : request.getPoints()) {
			String cpId = point.getClientPointId();
			LocationSource normalizedSource = normalizeSource(point.getSource());
			String dedupKey = buildDedupKey(subjectDevice.getDeviceId(), point, normalizedSource);

			if (existingIds.contains(cpId)
					|| !seenInBatch.add(cpId)
					|| existingDedupKeys.contains(dedupKey)
					|| !seenDedupInBatch.add(dedupKey)) {
				duplicates++;
				continue;
			}

			LocationLog log = new LocationLog();
			log.setRecordedAt(point.getRecordedAt());
			log.setReceivedAt(receivedAt);
			log.setAccuracy(point.getAccuracy());
			log.setSource(normalizedSource);
			log.setClientPointId(cpId);
			log.setDedupKey(dedupKey);
			log.setLocation(buildPoint(point.getLon(), point.getLat()));
			log.setSubjectDevice(subjectDevice);
			log.setUploaderDevice(uploaderDevice);

			toInsert.add(log);
		}

		int inserted = 0;
		int raceDuplicates = 0;
		List<LocationLog> insertedLogs = new ArrayList<>(toInsert.size());
		try {
			locationLogRepository.saveAll(toInsert);
			inserted = toInsert.size();
			insertedLogs.addAll(toInsert);
		} catch (DataIntegrityViolationException e) {
			for (LocationLog log : toInsert) {
				try {
					locationLogRepository.save(log);
					inserted++;
					insertedLogs.add(log);
				} catch (DataIntegrityViolationException ignored) {
					raceDuplicates++;
				}
			}
		}
		duplicates += raceDuplicates;

		upsertLatestLocation(insertedLogs);
		attachProjectionLocation(insertedLogs);

		if (!saveUploadBatchSafely(request.getClientBatchUuid(), totalReceived, receivedAt, uploaderDevice)) {
			return new LocationBatchUploadResponse(0, totalReceived, totalReceived);
		}

		return new LocationBatchUploadResponse(inserted, duplicates, totalReceived);
	}

	private void upsertLatestLocation(List<LocationLog> insertedLogs) {
		if (insertedLogs.isEmpty()) {
			return;
		}
		insertedLogs.stream()
				.sorted(Comparator
						.comparing(LocationLog::getRecordedAt)
						.thenComparing(LocationLog::getReceivedAt))
				.forEach(this::upsertProjectionFromLogSafely);
	}

	private void upsertProjectionFromLogSafely(LocationLog logEntry) {
		if (logEntry == null
				|| logEntry.getSubjectDevice() == null
				|| logEntry.getSubjectDevice().getDeviceId() == null
				|| logEntry.getSubjectDevice().getOwnerUser() == null
				|| logEntry.getSubjectDevice().getOwnerUser().getUserId() == null
				|| logEntry.getUploaderDevice() == null
				|| logEntry.getUploaderDevice().getDeviceId() == null
				|| logEntry.getLocation() == null
				|| logEntry.getRecordedAt() == null
				|| logEntry.getReceivedAt() == null
				|| logEntry.getSource() == null) {
			return;
		}

		Long subjectDeviceId = logEntry.getSubjectDevice().getDeviceId();
		try {
			locationRepository.upsertLatestProjection(
					logEntry.getAccuracy(),
					logEntry.getLocation().getX(),
					logEntry.getLocation().getY(),
					logEntry.getReceivedAt(),
					logEntry.getRecordedAt(),
					logEntry.getSource().name(),
					Instant.now(),
					logEntry.getSubjectDevice().getOwnerUser().getUserId(),
					subjectDeviceId,
					logEntry.getUploaderDevice().getDeviceId()
			);
		} catch (DataIntegrityViolationException e) {
			log.warn("Projection upsert failed for subjectDeviceId={}", subjectDeviceId);
		}
	}

	private void attachProjectionLocation(List<LocationLog> insertedLogs) {
		if (insertedLogs.isEmpty()) {
			return;
		}

		Location projectionLocation = locationRepository
				.findBySubjectDevice_DeviceId(insertedLogs.get(0).getSubjectDevice().getDeviceId())
				.orElse(null);
		if (projectionLocation == null) {
			return;
		}

		for (LocationLog insertedLog : insertedLogs) {
			insertedLog.setProjectionLocation(projectionLocation);
		}
		locationLogRepository.saveAll(insertedLogs);
	}

	private boolean saveUploadBatchSafely(String clientBatchUuid, int pointsCount, Instant uploadedAt, Device uploaderDevice) {
		UploadBatch uploadBatch = new UploadBatch();
		uploadBatch.setClientBatchUuid(clientBatchUuid);
		uploadBatch.setPointsCount(pointsCount);
		uploadBatch.setUploadedAt(uploadedAt);
		uploadBatch.setUploaderDevice(uploaderDevice);
		try {
			uploadBatchRepository.save(uploadBatch);
			return true;
		} catch (DataIntegrityViolationException ignored) {
			return false;
		}
	}

	private void validateClientBatchUuid(String clientBatchUuid) {
		if (clientBatchUuid == null || clientBatchUuid.isBlank()) {
			throw new IllegalArgumentException("clientBatchUuid is required.");
		}
	}

	private Device resolveAuthenticatedUploader(Long authenticatedUserId, Long requestUploaderDeviceId) {
		if (authenticatedUserId == null) {
			throw new IllegalArgumentException("Authenticated user id is required.");
		}

		if (requestUploaderDeviceId != null) {
			Device requestedUploader = deviceRepository.findById(requestUploaderDeviceId)
					.orElseThrow(() -> new IllegalArgumentException("uploaderDeviceId not found: " + requestUploaderDeviceId));
			Long ownerUserId = requestedUploader.getOwnerUser() != null
					? requestedUploader.getOwnerUser().getUserId()
					: null;
			if (ownerUserId == null || !ownerUserId.equals(authenticatedUserId)) {
				throw new IllegalArgumentException(
						"uploaderDeviceId spoof detected. uploaderDeviceId must belong to authenticated user.");
			}
			return requestedUploader;
		}

		return deviceRepository.findAllByOwnerUser_UserId(authenticatedUserId).stream()
				.min(Comparator.comparing(Device::getDeviceId))
				.orElseThrow(() -> new IllegalArgumentException(
						"No device linked to authenticated user."));
	}

	private String normalizeSubjectPeerId(String subjectPeerId) {
		if (subjectPeerId == null || subjectPeerId.isBlank()) {
			throw new IllegalArgumentException("subjectPeerId is required.");
		}
		String normalized = subjectPeerId.trim().toLowerCase(Locale.ROOT);
		if (normalized.length() > 64) {
			throw new IllegalArgumentException("subjectPeerId length is invalid.");
		}
		return normalized;
	}

	private Point buildPoint(double lon, double lat) {
		return GEO_FACTORY.createPoint(new Coordinate(lon, lat));
	}

	private String buildDedupKey(Long subjectDeviceId, LocationPointDto point, LocationSource normalizedSource) {
		String canonical = String.format(
				Locale.ROOT,
				"%d|%s|%.6f|%.6f|%s",
				subjectDeviceId,
				point.getRecordedAt(),
				roundCoord(point.getLat()),
				roundCoord(point.getLon()),
				normalizedSource
		);
		return sha256Hex(canonical);
	}

	private LocationSource normalizeSource(LocationSource source) {
		if (source == LocationSource.SELF) {
			return LocationSource.MESH;
		}
		return source;
	}

	private double roundCoord(Double value) {
		return java.math.BigDecimal.valueOf(value)
				.setScale(6, RoundingMode.HALF_UP)
				.doubleValue();
	}

	private String sha256Hex(String input) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder(hash.length * 2);
			for (byte b : hash) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 algorithm unavailable", e);
		}
	}
}
