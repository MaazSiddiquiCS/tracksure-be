package com.tracksure_be.repository;

import com.tracksure_be.entity.DeviceLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface DeviceLinkRepository extends JpaRepository<DeviceLink, Long> {
	Optional<DeviceLink> findByFollowerUser_UserIdAndTargetDevice_DeviceId(Long followerUserId, Long targetDeviceId);
	List<DeviceLink> findAllByFollowerUser_UserId(Long followerUserId);
	List<DeviceLink> findAllByTargetDevice_OwnerUser_UserId(Long ownerUserId);
	List<DeviceLink> findAllByTargetDevice_DeviceId(Long targetDeviceId);
	boolean existsByFollowerUser_UserIdAndTargetDevice_DeviceId(Long followerUserId, Long targetDeviceId);
}

