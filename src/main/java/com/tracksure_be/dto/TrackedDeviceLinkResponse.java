package com.tracksure_be.dto;

import com.tracksure_be.enums.PermissionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackedDeviceLinkResponse {
    private Long linkId;
    private PermissionType permissionType;
    private DeviceBasicResponse device;
    private UserBasicResponse owner;
}
