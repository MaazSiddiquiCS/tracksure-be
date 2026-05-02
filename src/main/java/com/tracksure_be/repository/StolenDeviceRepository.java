package com.tracksure_be.repository;

import com.tracksure_be.entity.StolenDevice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StolenDeviceRepository extends JpaRepository<StolenDevice, Long> {

    /**
     * Find stolen device by device ID
     */
        Optional<StolenDevice> findByDeviceId(Long deviceId);

    /**
     * Find all stolen devices for a specific user with pagination
     */
    Page<StolenDevice> findAllByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    /**
     * Find all active (not recovered) stolen devices for a user
     */
    Page<StolenDevice> findAllByUserIdAndIsRecoveredFalseOrderByTimestampDesc(Long userId, Pageable pageable);

    /**
     * Find all active stolen devices globally (for analytics/heatmaps)
     */
    List<StolenDevice> findAllByIsRecoveredFalseOrderByTimestampDesc();

    /**
     * Check if a device has been reported as stolen
     */
        boolean existsByDeviceId(Long deviceId);

    /**
     * Count total stolen devices reported
     */
    long countByIsRecoveredFalse();

    /**
     * Count total recovered devices
     */
    long countByIsRecoveredTrue();

    /**
     * Get heatmap data: all active stolen device locations
     */
    @Query("""
            SELECT new map(
                sd.latitude as latitude,
                sd.longitude as longitude,
                COUNT(sd.id) as frequency,
                sd.timestamp as timestamp
            )
            FROM StolenDevice sd
            WHERE sd.isRecovered = false
            GROUP BY sd.latitude, sd.longitude, sd.timestamp
            ORDER BY sd.timestamp DESC
            """)
    List<java.util.Map<String, Object>> findHeatmapData();

    /**
     * Get danger zones: cities ranked by stolen device count
     */
    @Query("""
            SELECT new map(
                sd.city as city,
                COUNT(sd.id) as deviceCount,
                MAX(sd.timestamp) as lastReportedAt
            )
            FROM StolenDevice sd
            WHERE sd.isRecovered = false AND sd.city IS NOT NULL
            GROUP BY sd.city
            ORDER BY COUNT(sd.id) DESC
            """)
    Page<java.util.Map<String, Object>> findDangerZones(Pageable pageable);

    /**
     * Get movement path for a specific device over last 24 hours
     */
    @Query("""
            SELECT sd
            FROM StolenDevice sd
            WHERE sd.deviceId = :deviceId
            AND sd.timestamp >= :startTime
            ORDER BY sd.timestamp ASC
            """)
    List<StolenDevice> findDeviceMovementPath(
            @Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime
    );

    /**
     * Find all stolen devices within a radius (using simple distance calculation)
     * Note: For more sophisticated geospatial queries, use PostGIS with @Query nativeQuery
     */
    @Query(value = """
            SELECT sd.* FROM stolen_devices sd
            WHERE sd.is_recovered = false
            AND (6371 * acos(cos(radians(:latitude)) * cos(radians(sd.latitude)) * 
                cos(radians(sd.longitude) - radians(:longitude)) + 
                sin(radians(:latitude)) * sin(radians(sd.latitude)))) < :radiusKm
            ORDER BY sd.timestamp DESC
            """, nativeQuery = true)
    List<StolenDevice> findDevicesWithinRadius(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm
    );

    /**
     * Count devices within a radius (for safety score calculation)
     */
    @Query(value = """
            SELECT COUNT(sd.id) FROM stolen_devices sd
            WHERE sd.is_recovered = false
            AND (6371 * acos(cos(radians(:latitude)) * cos(radians(sd.latitude)) * 
                cos(radians(sd.longitude) - radians(:longitude)) + 
                sin(radians(:latitude)) * sin(radians(sd.latitude)))) < :radiusKm
            """, nativeQuery = true)
    long countDevicesWithinRadius(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm
    );

    /**
     * Get devices within a radius for area safety check
     */
    @Query(value = """
            SELECT sd.* FROM stolen_devices sd
            WHERE sd.is_recovered = false
            AND (6371 * acos(cos(radians(:latitude)) * cos(radians(sd.latitude)) * 
                cos(radians(sd.longitude) - radians(:longitude)) + 
                sin(radians(:latitude)) * sin(radians(sd.latitude)))) < :radiusKm
            LIMIT :limit
            """, nativeQuery = true)
    List<StolenDevice> findDevicesWithinRadiusLimit(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radiusKm") Double radiusKm,
            @Param("limit") Integer limit
    );

    /**
     * Get analytics stats: total stolen, recovered, and active devices
     */
    @Query("""
            SELECT new map(
                COUNT(CASE WHEN sd.isRecovered = false THEN 1 END) as activeStolenDevices,
                COUNT(CASE WHEN sd.isRecovered = true THEN 1 END) as recoveredDevices,
                COUNT(sd.id) as totalReports
            )
            FROM StolenDevice sd
            """)
    java.util.Map<String, Object> getAnalyticsStats();

    /**
     * Get city-wise stolen device distribution for analytics
     */
    @Query("""
            SELECT new map(
                sd.city as city,
                COUNT(sd.id) as count,
                COUNT(CASE WHEN sd.isRecovered = false THEN 1 END) as activeCount
            )
            FROM StolenDevice sd
            WHERE sd.city IS NOT NULL
            GROUP BY sd.city
            ORDER BY COUNT(sd.id) DESC
            """)
    Page<java.util.Map<String, Object>> getCityDistribution(Pageable pageable);

    boolean existsByDeviceIdAndIsRecoveredFalse(Long deviceId);Optional<StolenDevice> findTopByDeviceIdAndIsRecoveredFalseOrderByTimestampDesc(Long deviceId);
}
