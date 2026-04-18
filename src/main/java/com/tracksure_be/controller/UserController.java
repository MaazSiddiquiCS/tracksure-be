package com.tracksure_be.controller;

import com.tracksure_be.dto.UserRequest;
import com.tracksure_be.dto.UserResponse;
import com.tracksure_be.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Users", description = "User CRUD and test listing endpoints")
public class UserController {

    private final UserService userService;

    @GetMapping("/test/users")
    @Operation(summary = "[TEST] Get all users without access token (optional username filter)")
    public ResponseEntity<List<UserResponse>> getAllUsersPublicTest(
            @RequestParam(required = false) String username
    ) {
        return ResponseEntity.ok(userService.getAllUsers(username));
    }

    @GetMapping("/users")
    @Operation(summary = "[TEST] Admin get all users (optional username filter)")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestParam(required = false) String username
    ) {
        return ResponseEntity.ok(userService.getAllUsers(username));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "[TEST] Admin get user by id")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getById(userId));
    }

    @PutMapping("/users/{userId}")
    @Operation(summary = "[TEST] Admin update user")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserRequest request
    ) {
        return ResponseEntity.ok(userService.update(userId, request));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "[TEST] Admin delete user")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
