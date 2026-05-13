package com.tracksure_be.service;

import com.tracksure_be.dto.UserRequest;
import com.tracksure_be.dto.UserResponse;
import com.tracksure_be.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers(String username);

    Page<UserResponse> getAllUsersPaginated(String username, Pageable pageable);

    UserResponse getById(Long userId);

    UserResponse update(Long userId, UserRequest request);

    void delete(Long userId);

    UserResponse changeRole(Long userId, Role newRole);
}
