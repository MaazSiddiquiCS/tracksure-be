package com.tracksure_be.repository;

import com.tracksure_be.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findBySubjectDevice_DeviceId(Long subjectDeviceId);
    List<Location> findAllBySubjectDevice_OwnerUser_UserIdOrderByRecordedAtDesc(Long ownerUserId);

    @Modifying
    @Query(value = """
            INSERT INTO locations (
                accuracy,
                location,
                received_at,
                recorded_at,
                source,
                updated_at,
                last_location_log_id,
                owner_user_id,
                subject_device_id,
                uploader_device_id
            ) VALUES (
                :accuracy,
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326),
                :receivedAt,
                :recordedAt,
                :source,
                :updatedAt,
                :lastLocationLogId,
                :ownerUserId,
                :subjectDeviceId,
                :uploaderDeviceId
            )
            ON CONFLICT (subject_device_id) DO UPDATE SET
                accuracy = EXCLUDED.accuracy,
                location = EXCLUDED.location,
                received_at = EXCLUDED.received_at,
                recorded_at = EXCLUDED.recorded_at,
                source = EXCLUDED.source,
                updated_at = EXCLUDED.updated_at,
                last_location_log_id = EXCLUDED.last_location_log_id,
                owner_user_id = EXCLUDED.owner_user_id,
                uploader_device_id = EXCLUDED.uploader_device_id
            WHERE EXCLUDED.recorded_at > locations.recorded_at
               OR (EXCLUDED.recorded_at = locations.recorded_at AND EXCLUDED.received_at >= locations.received_at)
            """, nativeQuery = true)
    void upsertLatestProjection(
            @Param("accuracy") Double accuracy,
            @Param("lon") double lon,
            @Param("lat") double lat,
            @Param("receivedAt") Instant receivedAt,
            @Param("recordedAt") Instant recordedAt,
            @Param("source") String source,
            @Param("updatedAt") Instant updatedAt,
            @Param("lastLocationLogId") Long lastLocationLogId,
            @Param("ownerUserId") Long ownerUserId,
            @Param("subjectDeviceId") Long subjectDeviceId,
            @Param("uploaderDeviceId") Long uploaderDeviceId
    );
}
