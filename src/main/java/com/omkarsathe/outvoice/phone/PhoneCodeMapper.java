package com.omkarsathe.outvoice.phone;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PhoneCodeMapper {

    PhoneCodeResponse toResponse(PhoneCode phoneCode);
}
