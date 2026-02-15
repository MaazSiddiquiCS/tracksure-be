package com.tracksure_be.mapper;

import com.tracksure_be.dto.ProfileDTO;
import com.tracksure_be.entity.Profile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileDTO toDto(Profile profile);
}