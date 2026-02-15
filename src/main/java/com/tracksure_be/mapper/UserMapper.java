package com.tracksure_be.mapper;

import com.tracksure_be.dto.UserDTO;
import com.tracksure_be.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDto(User user);
}