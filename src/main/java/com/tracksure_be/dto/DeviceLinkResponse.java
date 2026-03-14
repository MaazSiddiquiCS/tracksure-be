package com.tracksure_be.dto;

import com.tracksure_be.enums.PermissionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceLinkResponse {
    private Long linkId;
    private PermissionType permissionType;
    private Long followerId;
    private Long targetDeviceId;
}

