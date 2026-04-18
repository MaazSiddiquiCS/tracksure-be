package com.tracksure_be.controller;

import com.tracksure_be.dto.DeviceLinkUpsertRequest;
import com.tracksure_be.dto.DeviceLinkUpsertResponse;
import com.tracksure_be.dto.DeviceResponse;
import com.tracksure_be.security.UserPrincipal;
import com.tracksure_be.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
@Tag(name = "Device", description = "Authenticated device registration/linking")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping("/link")
    @Operation(summary = "Link current app/device peer id to authenticated account")
    public ResponseEntity<DeviceLinkUpsertResponse> link(
            @Valid @RequestBody DeviceLinkUpsertRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long authenticatedUserId = principal != null ? principal.getUserId() : null;
        return ResponseEntity.ok(deviceService.link(request, authenticatedUserId));
    }

    @GetMapping("/me")
    @Operation(summary = "Get all devices of authenticated user")
    public ResponseEntity<List<DeviceResponse>> getMine(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long authenticatedUserId = principal != null ? principal.getUserId() : null;
        return ResponseEntity.ok(deviceService.getByAuthenticatedUser(authenticatedUserId));
    }
}
