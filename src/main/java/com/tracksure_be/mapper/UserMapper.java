package com.tracksure_be.mapper;

import com.tracksure_be.dto.UserRequest;
import com.tracksure_be.dto.UserResponse;
import com.tracksure_be.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);

    User toEntity(UserRequest request);
}