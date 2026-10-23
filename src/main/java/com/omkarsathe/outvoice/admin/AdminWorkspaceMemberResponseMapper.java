package com.omkarsathe.outvoice.admin;

import com.omkarsathe.outvoice.workspace.member.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AdminWorkspaceMemberResponseMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "phoneCode", source = "user.phoneCode.code")
    @Mapping(target = "mobile", source = "user.mobile")
    AdminWorkspaceMemberResponse toResponse(Member member);
}
