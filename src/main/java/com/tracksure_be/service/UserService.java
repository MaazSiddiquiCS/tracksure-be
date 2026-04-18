package com.tracksure_be.service;

import com.tracksure_be.dto.UserRequest;
import com.tracksure_be.dto.UserResponse;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers(String username);

    UserResponse getById(Long userId);

    UserResponse update(Long userId, UserRequest request);

    void delete(Long userId);
}
