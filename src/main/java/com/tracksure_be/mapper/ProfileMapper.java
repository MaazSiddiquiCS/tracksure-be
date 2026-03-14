package com.tracksure_be.mapper;

import com.tracksure_be.dto.ProfileRequest;
import com.tracksure_be.dto.ProfileResponse;
import com.tracksure_be.entity.Profile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    ProfileResponse toResponse(Profile profile);

    Profile toEntity(ProfileRequest request);
}