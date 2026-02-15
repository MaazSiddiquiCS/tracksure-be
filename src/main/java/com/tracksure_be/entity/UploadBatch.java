package com.tracksure_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
		name = "upload_batches",
		indexes = {
				@Index(name = "idx_upload_batches_uploader_device_id", columnList = "uploader_device_id"),
				@Index(name = "idx_upload_batches_client_batch_uuid", columnList = "client_batch_uuid")
		}
)
@Getter
@Setter
@NoArgsConstructor
public class UploadBatch {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "upload_batches_seq")
	@SequenceGenerator(name = "upload_batches_seq", sequenceName = "upload_batches_seq", allocationSize = 50)
	@Column(name = "batch_id", nullable = false, updatable = false)
	private Long batchId;

	@Column(name = "client_batch_uuid", nullable = false)
	private String clientBatchUuid;

	@Column(name = "points_count", nullable = false)
	private Integer pointsCount;

	@Column(name = "uploaded_at", nullable = false)
	private Instant uploadedAt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "uploader_device_id", nullable = false)
	private Device uploaderDevice;
}

