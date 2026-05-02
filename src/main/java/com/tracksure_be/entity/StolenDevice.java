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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "stolen_devices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_stolen_devices_device_id", columnNames = "device_id")
        },
        indexes = {
                @Index(name = "idx_stolen_devices_device_id", columnList = "device_id"),
                @Index(name = "idx_stolen_devices_user_id", columnList = "user_id"),
                @Index(name = "idx_stolen_devices_city", columnList = "city"),
                @Index(name = "idx_stolen_devices_is_recovered", columnList = "is_recovered"),
                @Index(name = "idx_stolen_devices_timestamp", columnList = "timestamp"),
                @Index(name = "idx_stolen_devices_lat_lon", columnList = "latitude,longitude")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class StolenDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stolen_devices_seq")
    @SequenceGenerator(name = "stolen_devices_seq", sequenceName = "stolen_devices_seq", allocationSize = 50)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "device_id", nullable = false, unique = true)
    private Long deviceId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "formatted_address", length = 500)
    private String formattedAddress;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "is_recovered", nullable = false)
    private Boolean isRecovered = false;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "recovery_timestamp")
    private LocalDateTime recoveryTimestamp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
