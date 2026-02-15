package com.tracksure_be.entity;

import com.tracksure_be.enums.DeviceStatus;
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

import java.time.Instant;

@Entity
@Table(
		name = "devices",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_devices_peer_id", columnNames = "peer_id")
		},
		indexes = {
				@Index(name = "idx_devices_owner_user_id", columnList = "owner_user_id"),
				@Index(name = "idx_devices_peer_id", columnList = "peer_id")
		}
)
@Getter
@Setter
@NoArgsConstructor
public class Device {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "devices_seq")
	@SequenceGenerator(name = "devices_seq", sequenceName = "devices_seq", allocationSize = 50)
	@Column(name = "device_id", nullable = false, updatable = false)
	private Long deviceId;

	@Column(name = "peer_id", nullable = false, length = 16)
	private String peerId;

	@Column(name = "device_name")
	private String deviceName;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private DeviceStatus status;

	@Column(name = "last_seen_at")
	private Instant lastSeenAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_user_id", nullable = false)
	private User ownerUser;
}

