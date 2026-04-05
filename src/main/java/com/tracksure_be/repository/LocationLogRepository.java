package com.tracksure_be.repository;

import com.tracksure_be.entity.LocationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Repository
public interface LocationLogRepository extends JpaRepository<LocationLog, Long> {

	List<LocationLog> findAllBySubjectDevice_DeviceIdAndRecordedAtBetweenOrderByRecordedAtAsc(
			Long subjectDeviceId,
			Instant from,
			Instant to
	);

	List<LocationLog> findAllByUploaderDevice_DeviceIdAndRecordedAtBetweenOrderByRecordedAtAsc(
			Long uploaderDeviceId,
			Instant from,
			Instant to
	);

	/**
	 * Returns the set of client_point_id values already stored for the given
	 * subject/uploader pair.  Used to pre-filter duplicates before a batch insert.
	 */
	@Query("SELECT l.clientPointId FROM LocationLog l " +
			"WHERE l.subjectDevice.deviceId = :subjectDeviceId " +
			"AND l.uploaderDevice.deviceId = :uploaderDeviceId " +
			"AND l.clientPointId IS NOT NULL")
	Set<String> findClientPointIdsBySubjectAndUploader(
			@Param("subjectDeviceId") Long subjectDeviceId,
			@Param("uploaderDeviceId") Long uploaderDeviceId
	);

	@Query("SELECT l.dedupKey FROM LocationLog l " +
			"WHERE l.subjectDevice.deviceId = :subjectDeviceId " +
			"AND l.dedupKey IN :dedupKeys")
	Set<String> findDedupKeysBySubjectAndDedupKeyIn(
			@Param("subjectDeviceId") Long subjectDeviceId,
			@Param("dedupKeys") Set<String> dedupKeys
	);
}

