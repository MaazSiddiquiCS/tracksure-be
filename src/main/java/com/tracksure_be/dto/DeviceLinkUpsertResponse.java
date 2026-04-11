package com.tracksure_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceLinkUpsertResponse {
    private Long deviceId;
    private String peerId;
    private String deviceName;
}
