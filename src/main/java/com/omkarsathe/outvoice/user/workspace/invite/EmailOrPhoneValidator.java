package com.omkarsathe.outvoice.user.workspace.invite;

import com.omkarsathe.outvoice.common.validation.EmailOrMobileRequired;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EmailOrPhoneValidator implements ConstraintValidator<EmailOrMobileRequired, CreateInviteRequest> {

    @Override
    public boolean isValid(CreateInviteRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // let @NotNull handle nullability if needed
        }

        boolean hasEmail = request.email() != null && !request.email().isBlank();
        boolean hasPhone = request.phoneCodeId() != null
                && request.mobile() != null && !request.mobile().isBlank();

        boolean valid = hasEmail || hasPhone;

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            "Either email or both phoneCodeId and mobile must be provided")
                    .addConstraintViolation();
        }

        return valid;
    }
}
