package com.tracksure_be.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for formal theft report for authorities
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TheftReportDto {

    private Long reportId; // Device ID as report reference
    private String deviceDescription;
    private String reporterName;
    private String reporterContact;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reportedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime incidentTime;

    private String incidentLocation; // Last known location
    private Double incidentLatitude;
    private Double incidentLongitude;
    private String incidentCity;

    private Boolean hasBeenRecovered;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime recoveryDate;

    private String recoveryLocation;
    private Double recoveryLatitude;
    private Double recoveryLongitude;

    private String additionalNotes;
}
