package com.omkarsathe.outvoice.security;

import java.lang.annotation.*;

/**
 * Marks a controller method parameter to be resolved as the authenticated {@link UserContext}.
 * Usage: {@code public ResponseEntity<?> myEndpoint(@CurrentUser UserContext ctx) { ... }}
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {}
