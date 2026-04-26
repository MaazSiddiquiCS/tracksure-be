package com.tracksure_be.repository;

import com.tracksure_be.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository

public interface ProfileRepository extends JpaRepository<Profile, Long> {
	Optional<Profile> findByUser_UserId(Long userId);
	Optional<Profile> findByPhoneNumber(String phoneNumber);
	boolean existsByPhoneNumber(String phoneNumber);
	boolean existsByUser_UserId(Long userId);
	boolean existsByPhoneNumberAndUser_UserIdNot(String phoneNumber, Long userId);
}

