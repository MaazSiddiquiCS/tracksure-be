package com.tracksure_be.repository;

import com.tracksure_be.entity.UploadBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository

public interface UploadBatchRepository extends JpaRepository<UploadBatch, Long> {
	Optional<UploadBatch> findByClientBatchUuid(String clientBatchUuid);
	Optional<UploadBatch> findByUploaderDevice_DeviceIdAndClientBatchUuid(Long uploaderDeviceId, String clientBatchUuid);
	boolean existsByUploaderDevice_DeviceIdAndClientBatchUuid(Long uploaderDeviceId, String clientBatchUuid);
	List<UploadBatch> findAllByUploaderDevice_DeviceId(Long uploaderDeviceId);
}

