package com.omkarsathe.outvoice.workspace.customer;

import com.omkarsathe.outvoice.phone.PhoneCode;
import com.omkarsathe.outvoice.phone.PhoneCodeResponse;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String fullName,
        String email,
        PhoneCodeResponse phoneCode,
        String mobile
) {}
