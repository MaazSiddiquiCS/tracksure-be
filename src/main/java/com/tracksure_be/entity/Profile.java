package com.tracksure_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
		name = "profiles",
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_profiles_roll_number", columnNames = "roll_number"),
				@UniqueConstraint(name = "uk_profiles_user_id", columnNames = "user_id")
		},
		indexes = {
				@Index(name = "idx_profiles_department", columnList = "department")
		}
)
@Getter
@Setter
@NoArgsConstructor
public class Profile {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "profiles_seq")
	@SequenceGenerator(name = "profiles_seq", sequenceName = "profiles_seq", allocationSize = 50)
	@Column(name = "profile_id", nullable = false, updatable = false)
	private Long profileId;

	@Column(name = "full_name")
	private String fullName;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "bio")
	private String bio;

	@Column(name = "profile_pic", columnDefinition = "TEXT")
	private String profilePic;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
}

