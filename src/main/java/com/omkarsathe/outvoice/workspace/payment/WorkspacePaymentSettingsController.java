package com.omkarsathe.outvoice.workspace.payment;

import com.omkarsathe.outvoice.workspace.CurrentWorkspaceUser;
import com.omkarsathe.outvoice.workspace.Permission;
import com.omkarsathe.outvoice.workspace.WorkspacePrincipal;
import com.omkarsathe.outvoice.workspace.payment.dto.UpdateWorkspacePaymentSettingsRequest;
import com.omkarsathe.outvoice.workspace.payment.dto.WorkspacePaymentSettingsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/settings/payments/razorpay")
@RequiredArgsConstructor
public class WorkspacePaymentSettingsController {

    private final WorkspacePaymentSettingsService paymentSettingsService;

    @GetMapping
    public ResponseEntity<WorkspacePaymentSettingsResponse> getSettings(
            @PathVariable UUID workspaceId,
            @CurrentWorkspaceUser(requires = Permission.SETTINGS_VIEW) WorkspacePrincipal principal
    ) {
        WorkspacePaymentSettingsResponse response = paymentSettingsService.getSettings(workspaceId);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<WorkspacePaymentSettingsResponse> updateSettings(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody UpdateWorkspacePaymentSettingsRequest request,
            @CurrentWorkspaceUser(requires = Permission.SETTINGS_EDIT) WorkspacePrincipal principal
    ) {
        return ResponseEntity.ok(paymentSettingsService.updateSettings(workspaceId, request));
    }

    @DeleteMapping
    public ResponseEntity<Void> disconnectSettings(
            @PathVariable UUID workspaceId,
            @CurrentWorkspaceUser(requires = Permission.SETTINGS_EDIT) WorkspacePrincipal principal
    ) {
        paymentSettingsService.disconnectSettings(workspaceId);
        return ResponseEntity.noContent().build();
    }
}
