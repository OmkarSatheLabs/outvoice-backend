package com.omkarsathe.outvoice.security;

import com.omkarsathe.outvoice.organization.Permission;

import java.lang.annotation.*;

/**
 * Declares that the caller must hold at least one of the listed permissions.
 * OWNER and SUPER_USER pass automatically. Applied via {@link OrgAuthorizationAspect}.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {
    Permission[] value();
}
