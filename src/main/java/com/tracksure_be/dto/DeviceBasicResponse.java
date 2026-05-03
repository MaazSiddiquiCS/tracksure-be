package com.tracksure_be.dto;

import com.tracksure_be.enums.DeviceStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceBasicResponse {
    private Long deviceId;
    private String peerId;
    private String deviceName;
    private DeviceStatus status;
    private Instant lastSeenAt;
}
