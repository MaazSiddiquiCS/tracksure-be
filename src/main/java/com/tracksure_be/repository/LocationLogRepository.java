package com.tracksure_be.repository;

import com.tracksure_be.entity.LocationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
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
}

