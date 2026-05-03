package com.tracksure_be.dto;

import com.tracksure_be.enums.PermissionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceLinkCreateRequest {
    private Long targetDeviceId;
    private String peerId;
    private String deviceName;
    private PermissionType permissionType;
}
