package com.omkarsathe.outvoice.platform;

import java.util.UUID;

/** Immutable snapshot of an authenticated platform operator's identity, extracted from the platform JWT. */
public record PlatformContext(UUID platformUserId, PlatformRole role) {}
