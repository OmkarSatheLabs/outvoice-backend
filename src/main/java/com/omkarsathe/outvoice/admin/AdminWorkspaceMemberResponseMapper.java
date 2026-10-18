package com.omkarsathe.outvoice.admin;

import com.omkarsathe.outvoice.workspace.member.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminWorkspaceMemberResponseMapper {

    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "email", source = "user.email")
    AdminWorkspaceMemberResponse toResponse(Member member);
}
