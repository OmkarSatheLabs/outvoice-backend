package com.omkarsathe.outvoice.workspace.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserProfileResponseMapper {
    UserProfileResponse toUserProfileResponse(User user);
}
