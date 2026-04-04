package com.tracksure_be.entity;

import com.tracksure_be.enums.LocationSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.locationtech.jts.geom.Point;

import java.time.Instant;

@Entity
@Table(
		name = "location_logs",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_location_logs_idempotency",
						columnNames = {"subject_device_id", "uploader_device_id", "client_point_id"}
				)
		},
		indexes = {
				@Index(name = "idx_location_logs_recorded_at", columnList = "recorded_at"),
				@Index(name = "idx_location_logs_subject_device_id", columnList = "subject_device_id"),
				@Index(name = "idx_location_logs_uploader_device_id", columnList = "uploader_device_id"),
				@Index(name = "idx_location_logs_subject_recorded_at", columnList = "subject_device_id, recorded_at")
		}
)
@Getter
@Setter
@NoArgsConstructor
public class LocationLog {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "location_logs_seq")
	@SequenceGenerator(name = "location_logs_seq", sequenceName = "location_logs_seq", allocationSize = 50)
	@Column(name = "location_id", nullable = false, updatable = false)
	private Long locationId;

	@Column(name = "recorded_at", nullable = false)
	private Instant recordedAt;

	@Column(name = "received_at", nullable = false)
	private Instant receivedAt;

	@Column(name = "accuracy")
	private Double accuracy;

	@Enumerated(EnumType.STRING)
	@Column(name = "source", nullable = false)
	private LocationSource source;

	/**
	 * Stored as PostGIS geometry(Point,4326).
	 * Note: spatial indexes are typically created via a GIST index in migrations.
	 */
	@Column(name = "location", nullable = false, columnDefinition = "geometry(Point,4326)")
	private Point location;

	/**
	 * Client-assigned identifier for idempotency.
	 * Combined with subject_device_id and uploader_device_id to form a unique constraint,
	 * preventing duplicate inserts on offline/retry uploads.
	 */
	@Column(name = "client_point_id", length = 255)
	private String clientPointId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "subject_device_id", nullable = false)
	private Device subjectDevice;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "uploader_device_id", nullable = false)
	private Device uploaderDevice;
}

