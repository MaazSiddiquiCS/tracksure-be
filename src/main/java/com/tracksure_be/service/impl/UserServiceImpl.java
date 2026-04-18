package com.tracksure_be.service.impl;

import com.tracksure_be.dto.UserRequest;
import com.tracksure_be.dto.UserResponse;
import com.tracksure_be.entity.User;
import com.tracksure_be.exception.EmailAlreadyExistsException;
import com.tracksure_be.exception.UserNotFoundException;
import com.tracksure_be.exception.UsernameAlreadyExistsException;
import com.tracksure_be.mapper.UserMapper;
import com.tracksure_be.repository.UserRepository;
import com.tracksure_be.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(String username) {
        List<User> users;
        if (username != null && !username.isBlank()) {
            users = userRepository.findAllByUsernameContainingIgnoreCaseOrderByUsernameAsc(username.trim());
        } else {
            users = userRepository.findAllByOrderByUsernameAsc();
        }
        return users.stream().map(userMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse update(Long userId, UserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String incomingUsername = request.getUsername() != null ? request.getUsername().trim() : null;
        String incomingEmail = request.getEmail() != null ? request.getEmail().trim() : null;

        if (incomingUsername == null || incomingUsername.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (incomingEmail == null || incomingEmail.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }

        userRepository.findByUsername(incomingUsername)
                .filter(found -> !found.getUserId().equals(userId))
                .ifPresent(found -> {
                    throw new UsernameAlreadyExistsException("Username already exists: " + incomingUsername);
                });

        userRepository.findByEmail(incomingEmail)
                .filter(found -> !found.getUserId().equals(userId))
                .ifPresent(found -> {
                    throw new EmailAlreadyExistsException("Email already exists: " + incomingEmail);
                });

        user.setUsername(incomingUsername);
        user.setEmail(incomingEmail);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        userRepository.deleteById(userId);
    }
}
