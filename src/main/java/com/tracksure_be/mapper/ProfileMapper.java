package com.tracksure_be.mapper;

import com.tracksure_be.dto.ProfileRequest;
import com.tracksure_be.dto.ProfileResponse;
import com.tracksure_be.entity.Profile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProfileMapper {
    @Mapping(source = "user.userId", target = "userId")
    ProfileResponse toResponse(Profile profile);

    @Mapping(target = "profileId", ignore = true)
    @Mapping(target = "user", ignore = true)
    Profile toEntity(ProfileRequest request);
}