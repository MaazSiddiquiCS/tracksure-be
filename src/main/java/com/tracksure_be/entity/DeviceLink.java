package com.tracksure_be.entity;

import com.tracksure_be.enums.PermissionType;
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

@Entity
@Table(
		name = "device_links",
		uniqueConstraints = {
				@UniqueConstraint(
						name = "uk_device_links_follower_target",
						columnNames = {"follower_id", "target_device_id"}
				)
		},
		indexes = {
				@Index(name = "idx_device_links_follower_id", columnList = "follower_id"),
				@Index(name = "idx_device_links_target_device_id", columnList = "target_device_id")
		}
)
@Getter
@Setter
@NoArgsConstructor
public class DeviceLink {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "device_links_seq")
	@SequenceGenerator(name = "device_links_seq", sequenceName = "device_links_seq", allocationSize = 50)
	@Column(name = "link_id", nullable = false, updatable = false)
	private Long linkId;

	@Enumerated(EnumType.STRING)
	@Column(name = "permission_type", nullable = false)
	private PermissionType permissionType;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "follower_id", nullable = false)
	private User followerUser;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "target_device_id", nullable = false)
	private Device targetDevice;
}

