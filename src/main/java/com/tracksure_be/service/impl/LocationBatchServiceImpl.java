package com.tracksure_be.service.impl;

import com.tracksure_be.dto.LocationBatchUploadRequest;
import com.tracksure_be.dto.LocationBatchUploadResponse;
import com.tracksure_be.dto.LocationPointDto;
import com.tracksure_be.entity.Device;
import com.tracksure_be.entity.LocationLog;
import com.tracksure_be.entity.UploadBatch;
import com.tracksure_be.enums.LocationSource;
import com.tracksure_be.repository.DeviceRepository;
import com.tracksure_be.repository.LocationLogRepository;
import com.tracksure_be.repository.UploadBatchRepository;
import com.tracksure_be.service.LocationBatchService;
import lombok.RequiredArgsConstructor;
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
public class LocationBatchServiceImpl implements LocationBatchService {

	private static final GeometryFactory GEO_FACTORY =
			new GeometryFactory(new PrecisionModel(), 4326);

	private final LocationLogRepository locationLogRepository;
	private final DeviceRepository deviceRepository;
	private final UploadBatchRepository uploadBatchRepository;

	@Override
	@Transactional
	public LocationBatchUploadResponse uploadBatch(LocationBatchUploadRequest request, Long authenticatedUserId) {
		Device uploaderDevice = resolveAuthenticatedUploader(authenticatedUserId);
		rejectSpoofedUploaderIfPresent(request, uploaderDevice);
		validateClientBatchUuid(request.getClientBatchUuid());
        String normalizedSubjectPeerId = normalizeSubjectPeerId(request.getSubjectPeerId());

		if (uploadBatchRepository.existsByUploaderDevice_DeviceIdAndClientBatchUuid(
				uploaderDevice.getDeviceId(), request.getClientBatchUuid())) {
			int total = request.getPoints() != null ? request.getPoints().size() : 0;
			return new LocationBatchUploadResponse(0, total, total);
		}

		Device subjectDevice = deviceRepository.findByPeerId(normalizedSubjectPeerId)
				.orElseThrow(() -> new IllegalArgumentException(
						"Subject device not found for peerId: " + normalizedSubjectPeerId));

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

		int totalReceived = request.getPoints().size();
		int duplicates = 0;

		Instant receivedAt = Instant.now();
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
		try {
			locationLogRepository.saveAll(toInsert);
			inserted = toInsert.size();
		} catch (DataIntegrityViolationException e) {
			for (LocationLog log : toInsert) {
				try {
					locationLogRepository.save(log);
					inserted++;
				} catch (DataIntegrityViolationException ignored) {
					raceDuplicates++;
				}
			}
		}
		duplicates += raceDuplicates;

		UploadBatch uploadBatch = new UploadBatch();
		uploadBatch.setClientBatchUuid(request.getClientBatchUuid());
		uploadBatch.setPointsCount(totalReceived);
		uploadBatch.setUploadedAt(receivedAt);
		uploadBatch.setUploaderDevice(uploaderDevice);
		try {
			uploadBatchRepository.save(uploadBatch);
		} catch (DataIntegrityViolationException ignored) {
			return new LocationBatchUploadResponse(0, totalReceived, totalReceived);
		}

		return new LocationBatchUploadResponse(inserted, duplicates, totalReceived);
	}

	private void validateClientBatchUuid(String clientBatchUuid) {
		if (clientBatchUuid == null || clientBatchUuid.isBlank()) {
			throw new IllegalArgumentException("clientBatchUuid is required.");
		}
	}

	private Device resolveAuthenticatedUploader(Long authenticatedUserId) {
		if (authenticatedUserId == null) {
			throw new IllegalArgumentException("Authenticated user id is required.");
		}

		return deviceRepository.findAllByOwnerUser_UserId(authenticatedUserId).stream()
				.min(Comparator.comparing(Device::getDeviceId))
				.orElseThrow(() -> new IllegalArgumentException(
						"No device linked to authenticated user."));
	}

	private void rejectSpoofedUploaderIfPresent(LocationBatchUploadRequest request, Device authenticatedUploader) {
		if (request.getUploaderDeviceId() != null
				&& !request.getUploaderDeviceId().equals(authenticatedUploader.getDeviceId())) {
			throw new IllegalArgumentException(
					"uploaderDeviceId spoof detected. uploaderDeviceId is server-derived from authenticated principal.");
		}
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
