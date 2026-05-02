package com.tracksure_be.service;

import com.tracksure_be.dto.AnalyticsStatsDto;
import com.tracksure_be.dto.DangerZoneDto;
import com.tracksure_be.dto.DeviceMovementPointDto;
import com.tracksure_be.dto.HeatmapPointDto;
import com.tracksure_be.dto.ReportStolenDeviceRequest;
import com.tracksure_be.dto.SafetyScoreDto;
import com.tracksure_be.dto.StolenDeviceResponse;
import com.tracksure_be.dto.TheftReportDto;
import com.tracksure_be.entity.StolenDevice;
import com.tracksure_be.entity.User;
import com.tracksure_be.exception.DeviceAlreadyReportedException;
import com.tracksure_be.exception.InvalidCoordinatesException;
import com.tracksure_be.exception.StolenDeviceNotFoundException;
import com.tracksure_be.repository.StolenDeviceRepository;
import com.tracksure_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing stolen device reports and analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StolenDeviceService {

    private final StolenDeviceRepository stolenDeviceRepository;
    private final UserRepository userRepository;
    private final GeocodingService geocodingService;

    private static final Double SAFETY_CHECK_RADIUS_KM = 5.0;

    /**
     * Report a stolen/lost device with location and geocoding
     */
    @Transactional
    public StolenDeviceResponse reportStolenDevice(ReportStolenDeviceRequest request, Long userId) {
        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate coordinates
        if (!geocodingService.isValidCoordinate(request.getLatitude(), request.getLongitude())) {
            throw new InvalidCoordinatesException("Invalid coordinates provided. Latitude must be between -90 and 90, Longitude between -180 and 180.");
        }

        // Check if device already reported
        if (stolenDeviceRepository.existsByDeviceIdAndIsRecoveredFalse(request.getDeviceId())) {
            throw new DeviceAlreadyReportedException("Device with ID " + request.getDeviceId() + " has already been reported as stolen.");
        }

        // Reverse geocode to get address and city
        Map<String, Object> geoData = geocodingService.reverseGeocode(
                request.getLatitude(),
                request.getLongitude()
        );

        // Create and populate stolen device entity
        StolenDevice stolenDevice = new StolenDevice();
        stolenDevice.setDeviceId(request.getDeviceId());
        stolenDevice.setUserId(userId);
        stolenDevice.setLatitude(request.getLatitude());
        stolenDevice.setLongitude(request.getLongitude());
        stolenDevice.setIsRecovered(false);
        stolenDevice.setTimestamp(LocalDateTime.now());

        // Set geocoded address data if successful
        if ((Boolean) geoData.getOrDefault("success", false)) {
            stolenDevice.setFormattedAddress((String) geoData.get("formattedAddress"));
            stolenDevice.setCity((String) geoData.get("city"));
            log.info("Geocoding successful for device {}. City: {}", request.getDeviceId(), 
                    geoData.get("city"));
        } else {
            log.warn("Geocoding failed for device {}: {}", request.getDeviceId(), 
                    geoData.get("error"));
        }

        StolenDevice saved = stolenDeviceRepository.save(stolenDevice);
        return mapToResponse(saved);
    }

    /**
     * Get stolen devices for authenticated user with pagination
     */
    @Transactional(readOnly = true)
    public Page<StolenDeviceResponse> getMyDevices(Long userId, Pageable pageable) {
        Page<StolenDevice> devices = stolenDeviceRepository.findAllByUserIdOrderByTimestampDesc(
                userId, pageable
        );
        return devices.map(this::mapToResponse);
    }

    /**
     * Get details of a specific stolen device
     */
    @Transactional(readOnly = true)
    public StolenDeviceResponse getDeviceDetails(Long deviceId, Long userId) {
        StolenDevice device = stolenDeviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new StolenDeviceNotFoundException("Stolen device with ID " + deviceId + " not found"));

        // Ensure user can only access their own devices
        if (!device.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to this device");
        }

        return mapToResponse(device);
    }

    /**
     * Mark a device as recovered
     */
    @Transactional
    public StolenDeviceResponse recoverDevice(Long deviceId, Long userId) throws AccessDeniedException {
        StolenDevice device = stolenDeviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new StolenDeviceNotFoundException(
                        "Stolen device with ID " + deviceId + " not found"
                ));

        if (!device.getUserId().equals(userId)) {
            throw new AccessDeniedException("You are not allowed to recover this device");
        }

        if (device.getIsRecovered()) {
            throw new IllegalStateException("Device is already marked as recovered");
        }

        device.setIsRecovered(true);
        device.setRecoveryTimestamp(LocalDateTime.now());

        StolenDevice updated = stolenDeviceRepository.save(device);

        log.info("Device {} marked as recovered", deviceId);
        return mapToResponse(updated);
    }
    /**
     * Get heatmap data - concentration points of stolen devices
     */
    @Transactional(readOnly = true)
    public List<HeatmapPointDto> getHeatmapData() {
        List<Map<String, Object>> heatmapData = stolenDeviceRepository.findHeatmapData();
        return heatmapData.stream()
                .map(this::convertToHeatmapPoint)
                .collect(Collectors.toList());
    }

    /**
     * Get danger zones - cities ranked by stolen device frequency
     */
    @Transactional(readOnly = true)
    public Page<DangerZoneDto> getDangerZones(Pageable pageable) {
        Page<Map<String, Object>> dangerZones = stolenDeviceRepository.findDangerZones(pageable);
        return dangerZones.map(this::convertToDangerZone);
    }

    /**
     * Get movement path for a device over the last 24 hours
     */
    @Transactional(readOnly = true)
    public List<DeviceMovementPointDto> getDeviceMovementPath(Long deviceId, Long userId) {
        // Verify device belongs to user
        StolenDevice device = stolenDeviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new StolenDeviceNotFoundException("Stolen device with ID " + deviceId + " not found"));

        if (!device.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to this device");
        }

        LocalDateTime startTime = LocalDateTime.now().minusHours(24);
        List<StolenDevice> path = stolenDeviceRepository.findDeviceMovementPath(deviceId, startTime);

        return path.stream()
                .map(this::mapToMovementPoint)
                .collect(Collectors.toList());
    }

    /**
     * Get global analytics statistics
     */
    @Transactional(readOnly = true)
    public AnalyticsStatsDto getAnalyticsStats() {
        Map<String, Object> stats = stolenDeviceRepository.getAnalyticsStats();

        long totalStolenReports = ((Number) stats.getOrDefault("totalReports", 0L)).longValue();
        long recoveredDevices = ((Number) stats.getOrDefault("recoveredDevices", 0L)).longValue();
        long activeStolenDevices = ((Number) stats.getOrDefault("activeStolenDevices", 0L)).longValue();

        double recoveryRate = totalStolenReports > 0 
            ? (recoveredDevices * 100.0) / totalStolenReports 
            : 0.0;

        return AnalyticsStatsDto.builder()
                .totalStolenReports(totalStolenReports)
                .totalRecovered(recoveredDevices)
                .activeDevices(activeStolenDevices)
                .recoveryRate(Math.round(recoveryRate * 100.0) / 100.0)
                .build();
    }

    /**
     * Check safety score for an area
     */
    @Transactional(readOnly = true)
    public SafetyScoreDto checkAreaSafety(Double latitude, Double longitude) {
        // Validate coordinates
        if (!geocodingService.isValidCoordinate(latitude, longitude)) {
            throw new InvalidCoordinatesException("Invalid coordinates provided. Latitude must be between -90 and 90, Longitude between -180 and 180.");
        }

        long devicesInRadius = stolenDeviceRepository.countDevicesWithinRadius(
                latitude, longitude, SAFETY_CHECK_RADIUS_KM
        );

        // Calculate safety score (0-100, where 100 is safest)
        // Using threshold of 10 devices for "safe", 50+ for "danger"
        int safetyScore = calculateSafetyScore(devicesInRadius);
        String safetyLevel = determineSafetyLevel(devicesInRadius);
        String recommendation = generateRecommendation(safetyLevel, devicesInRadius);

        return SafetyScoreDto.builder()
                .latitude(latitude)
                .longitude(longitude)
                .safetyScore(safetyScore)
                .safetyLevel(safetyLevel)
                .devicesInArea(devicesInRadius)
                .radiusKm(SAFETY_CHECK_RADIUS_KM)
                .recommendation(recommendation)
                .build();
    }

    /**
     * Generate formal theft report for authorities
     */
    @Transactional(readOnly = true)
    public TheftReportDto generatePoliceReport(Long deviceId, Long userId) {
        StolenDevice device = stolenDeviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new StolenDeviceNotFoundException("Stolen device with ID " + deviceId + " not found"));

        if (!device.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized access to this device");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        TheftReportDto report = new TheftReportDto();
        report.setReportId(device.getDeviceId());
        report.setDeviceDescription("BLE Tracking Device - " + device.getDeviceId());
        report.setReporterName(user.getUsername());
        report.setReporterContact(user.getEmail());
        report.setReportedAt(device.getCreatedAt());
        report.setIncidentTime(device.getTimestamp());
        report.setIncidentLocation(device.getFormattedAddress());
        report.setIncidentLatitude(device.getLatitude());
        report.setIncidentLongitude(device.getLongitude());
        report.setIncidentCity(device.getCity());
        report.setHasBeenRecovered(device.getIsRecovered());

        if (device.getIsRecovered()) {
            report.setRecoveryDate(device.getRecoveryTimestamp());
            report.setRecoveryLocation("Device marked as recovered");
        }

        report.setAdditionalNotes("Device reported stolen/lost through TrackSure BLE Tracking System");

        return report;
    }

    /**
     * Helper method to map StolenDevice to StolenDeviceResponse DTO
     */
    private StolenDeviceResponse mapToResponse(StolenDevice device) {
        return StolenDeviceResponse.builder()
                .id(device.getId())
                .deviceId(device.getDeviceId())
                .latitude(device.getLatitude())
                .longitude(device.getLongitude())
                .formattedAddress(device.getFormattedAddress())
                .city(device.getCity())
                .isRecovered(device.getIsRecovered())
                .timestamp(device.getTimestamp())
                .recoveryTimestamp(device.getRecoveryTimestamp())
                .createdAt(device.getCreatedAt())
                .updatedAt(device.getUpdatedAt())
                .build();
    }

    /**
     * Convert map data to HeatmapPointDto
     */
    private HeatmapPointDto convertToHeatmapPoint(Map<String, Object> data) {
        Double latitude = ((Number) data.get("latitude")).doubleValue();
        Double longitude = ((Number) data.get("longitude")).doubleValue();
        Long frequency = ((Number) data.get("frequency")).longValue();
        LocalDateTime timestamp = (LocalDateTime) data.get("timestamp");

        // Calculate intensity score (0-1) based on frequency
        // Normalize to 0-1 scale
        double maxFrequency = 100.0; // Adjust based on your typical frequency distribution
        double intensity = Math.min(1.0, frequency / maxFrequency);

        return HeatmapPointDto.builder()
                .latitude(latitude)
                .longitude(longitude)
                .frequency(frequency)
                .intensity(intensity)
                .lastReportedAt(timestamp)
                .build();
    }

    /**
     * Convert map data to DangerZoneDto
     */
    private DangerZoneDto convertToDangerZone(Map<String, Object> data) {
        String city = (String) data.get("city");
        Long deviceCount = ((Number) data.get("deviceCount")).longValue();
        LocalDateTime lastReportedAt = (LocalDateTime) data.get("lastReportedAt");

        int riskLevel = calculateRiskLevel(deviceCount);
        String riskDescription = determineRiskDescription(deviceCount);

        return DangerZoneDto.builder()
                .city(city)
                .deviceCount(deviceCount)
                .riskLevel(riskLevel)
                .lastReportedAt(lastReportedAt)
                .riskDescription(riskDescription)
                .build();
    }

    /**
     * Convert StolenDevice to DeviceMovementPointDto
     */
    private DeviceMovementPointDto mapToMovementPoint(StolenDevice device) {
        return DeviceMovementPointDto.builder()
                .latitude(device.getLatitude())
                .longitude(device.getLongitude())
                .timestamp(device.getTimestamp())
                .formattedAddress(device.getFormattedAddress())
                .city(device.getCity())
                .build();
    }

    /**
     * Calculate safety score based on devices in area
     */
    private int calculateSafetyScore(long devicesInArea) {
        // Score formula: 100 - (devices * 2), with minimum of 0
        return Math.max(0, (int) (100 - (devicesInArea * 2)));
    }

    /**
     * Determine safety level based on device count
     */
    private String determineSafetyLevel(long devicesInArea) {
        if (devicesInArea < 10) {
            return "Safe";
        } else if (devicesInArea < 50) {
            return "Caution";
        } else {
            return "Danger";
        }
    }

    /**
     * Calculate risk level (1-10 scale)
     */
    private int calculateRiskLevel(long deviceCount) {
        return Math.min(10, (int) (1 + (deviceCount / 5)));
    }

    /**
     * Determine risk description
     */
    private String determineRiskDescription(long deviceCount) {
        if (deviceCount < 5) {
            return "Low Risk";
        } else if (deviceCount < 20) {
            return "Medium Risk";
        } else if (deviceCount < 50) {
            return "High Risk";
        } else {
            return "Critical Risk";
        }
    }

    /**
     * Generate safety recommendation
     */
    private String generateRecommendation(String safetyLevel, long devicesInArea) {
        return switch (safetyLevel) {
            case "Safe" -> "Area is safe. No recent theft reports in this location.";
            case "Caution" -> String.format("Exercise caution. %d stolen devices reported in this area within 5km.", devicesInArea);
            case "Danger" -> String.format("DANGER ZONE: %d stolen devices reported in this area within 5km. Avoid if possible and keep valuables secure.", devicesInArea);
            default -> "Unable to determine area safety";
        };
    }
}
