package com.omkarsathe.outvoice.security;

import com.omkarsathe.outvoice.organization.OrgRole;
import com.omkarsathe.outvoice.organization.Permission;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * AOP aspect that enforces {@link RequiresPermission} and {@link RequiresRole} annotations at
 * the service layer (defense-in-depth; controllers are also annotated).
 */
@Aspect
@Component
@Slf4j
public class OrgAuthorizationAspect {

    @Before("@annotation(requiresPermission)")
    public void checkPermission(RequiresPermission requiresPermission) {
        UserContext ctx = resolveContext();
        if (!ctx.hasOrgContext()) {
            throw new AccessDeniedException("No org context in token — call /auth/select-org first");
        }
        Permission[] needed = requiresPermission.value();
        for (Permission p : needed) {
            if (ctx.hasPermission(p)) return;
        }
        throw new AccessDeniedException("Missing required permission: " + Arrays.toString(needed));
    }

    @Before("@annotation(requiresRole)")
    public void checkRole(RequiresRole requiresRole) {
        UserContext ctx = resolveContext();
        if (!ctx.hasOrgContext()) {
            throw new AccessDeniedException("No org context in token — call /auth/select-org first");
        }
        OrgRole[] needed = requiresRole.value();
        if (!ctx.hasAnyRole(needed)) {
            throw new AccessDeniedException("Insufficient role. Required one of: " + Arrays.toString(needed));
        }
    }

    private UserContext resolveContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserContext ctx)) {
            throw new AccessDeniedException("Not authenticated");
        }
        return ctx;
    }
}
