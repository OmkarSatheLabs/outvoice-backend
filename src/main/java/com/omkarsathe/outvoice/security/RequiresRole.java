package com.omkarsathe.outvoice.security;

import com.omkarsathe.outvoice.organization.OrgRole;

import java.lang.annotation.*;

/**
 * Declares that the caller must hold at least one of the listed org roles.
 * Applied via {@link OrgAuthorizationAspect}.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresRole {
    OrgRole[] value();
}
