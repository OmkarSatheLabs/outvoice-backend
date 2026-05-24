package com.omkarsathe.outvoice.common.validation;

import com.omkarsathe.outvoice.auth.dto.SignupRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

public class EmailOrMobileRequiredValidator implements ConstraintValidator<EmailOrMobileRequired, SignupRequest> {

    @Override
    public boolean isValid(SignupRequest request, ConstraintValidatorContext context) {
        return StringUtils.hasText(request.getEmail()) || StringUtils.hasText(request.getMobile());
    }
}
