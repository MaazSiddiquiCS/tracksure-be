package com.tracksure_be.dto;

import com.tracksure_be.enums.LocationSource;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationPointDto {

	/**
	 * Client-assigned unique identifier for this point, used for idempotency.
	 * Must be stable across retries (e.g., a UUID generated on the device).
	 */
	@NotBlank(message = "clientPointId must not be blank")
	private String clientPointId;

	@NotNull(message = "lat must not be null")
	@DecimalMin(value = "-90.0", message = "lat must be >= -90")
	@DecimalMax(value = "90.0", message = "lat must be <= 90")
	private Double lat;

	@NotNull(message = "lon must not be null")
	@DecimalMin(value = "-180.0", message = "lon must be >= -180")
	@DecimalMax(value = "180.0", message = "lon must be <= 180")
	private Double lon;

	/** GPS accuracy in metres (optional). */
	private Double accuracy;

	@NotNull(message = "recordedAt must not be null")
	private Instant recordedAt;

	@NotNull(message = "source must not be null")
	private LocationSource source;
}
