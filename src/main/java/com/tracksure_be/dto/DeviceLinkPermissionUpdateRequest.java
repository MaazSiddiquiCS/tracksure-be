package com.tracksure_be.dto;

import com.tracksure_be.enums.PermissionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceLinkPermissionUpdateRequest {
    @NotNull(message = "permissionType must not be null")
    private PermissionType permissionType;
}
