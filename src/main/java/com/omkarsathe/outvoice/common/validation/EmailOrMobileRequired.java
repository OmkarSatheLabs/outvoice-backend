package com.omkarsathe.outvoice.common.validation;

import com.omkarsathe.outvoice.user.workspace.invite.EmailOrPhoneValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {EmailOrMobileRequiredValidator.class, EmailOrPhoneValidator.class})
@Documented
public @interface EmailOrMobileRequired {
    String message() default "Either email or mobile number must be provided";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
