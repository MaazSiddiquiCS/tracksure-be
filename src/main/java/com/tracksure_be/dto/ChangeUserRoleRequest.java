package com.tracksure_be.dto;

import com.tracksure_be.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for changing a user's role.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeUserRoleRequest {
    
    @NotNull(message = "Role is required")
    private Role role;
}
