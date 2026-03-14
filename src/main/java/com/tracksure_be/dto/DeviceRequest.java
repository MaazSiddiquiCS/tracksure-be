package com.tracksure_be.dto;

import com.tracksure_be.enums.DeviceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceRequest {
    private String peerId;
    private String deviceName;
    private DeviceStatus status;
    private Long ownerUserId;
}

