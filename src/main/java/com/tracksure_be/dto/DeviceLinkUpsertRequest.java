package com.tracksure_be.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceLinkUpsertRequest {

    @NotBlank(message = "peerId must not be blank")
    private String peerId;

    private String deviceName;
}
