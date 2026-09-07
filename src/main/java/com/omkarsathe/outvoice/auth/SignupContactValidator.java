package com.omkarsathe.outvoice.auth;

import com.omkarsathe.outvoice.workspace.user.CreateUser;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SignupContactValidator implements ConstraintValidator<ValidSignupContact, ContactRequest> {

    @Override
    public boolean isValid(ContactRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        boolean hasEmail = request.email() != null
                && !request.email().isBlank();

        boolean hasPhone = request.phoneCode() != null
                && !request.phoneCode().isBlank()
                && request.mobile() != null
                && !request.mobile().isBlank();

        boolean hasPartialPhone = (request.phoneCode() != null
                && !request.phoneCode().isBlank())
                ^ (request.mobile() != null
                && !request.mobile().isBlank());

        if (hasPartialPhone) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Phone code and mobile must be provided together"
                    ).addPropertyNode("mobile")
                    .addConstraintViolation();

            return false;
        }

        return hasEmail || hasPhone;
    }
}
