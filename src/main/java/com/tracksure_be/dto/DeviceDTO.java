package com.tracksure_be.dto;

import com.tracksure_be.enums.DeviceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceDTO {
    private Long deviceId;
    private String peerId;
    private String deviceName;
    private DeviceStatus status;
    private Instant lastSeenAt;
    private Long ownerUserId;
}