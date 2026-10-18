package com.omkarsathe.outvoice.admin;

import java.math.BigDecimal;
import java.util.UUID;

public interface WorkspaceBilledProjection {
    UUID getWorkspaceId();
    BigDecimal getTotal();
}
