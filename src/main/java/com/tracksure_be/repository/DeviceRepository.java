package com.tracksure_be.repository;

import com.tracksure_be.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository

public interface DeviceRepository extends JpaRepository<Device, Long> {
	Optional<Device> findByPeerId(String peerId);
	boolean existsByPeerId(String peerId);
	List<Device> findAllByOwnerUser_UserId(Long ownerUserId);
}

