package com.omkarsathe.outvoice.auth;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SignupContactValidator.class)
@Documented
public @interface ValidSignupContact {

    String message() default "Either email or phone code and mobile must be provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
