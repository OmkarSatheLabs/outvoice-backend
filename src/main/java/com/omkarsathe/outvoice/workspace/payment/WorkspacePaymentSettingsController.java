package com.omkarsathe.outvoice.workspace.payment;

import com.omkarsathe.outvoice.workspace.CurrentWorkspaceUser;
import com.omkarsathe.outvoice.workspace.Permission;
import com.omkarsathe.outvoice.workspace.WorkspacePrincipal;
import com.omkarsathe.outvoice.workspace.payment.dto.PaymentGatewayConfigResponse;
import com.omkarsathe.outvoice.workspace.payment.dto.UpdatePaymentGatewayConfigRequest;
import com.omkarsathe.outvoice.workspace.payment.dto.UpdateWorkspacePaymentSettingsRequest;
import com.omkarsathe.outvoice.workspace.payment.dto.WorkspacePaymentSettingsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/settings/payments")
@RequiredArgsConstructor
public class WorkspacePaymentSettingsController {

    private final WorkspacePaymentSettingsService paymentSettingsService;

    @GetMapping
    public ResponseEntity<List<PaymentGatewayConfigResponse>> getAllGateways(
            @PathVariable UUID workspaceId,
            @CurrentWorkspaceUser(requires = Permission.SETTINGS_VIEW) WorkspacePrincipal principal
    ) {
        return ResponseEntity.ok(paymentSettingsService.getAllGateways(workspaceId));
    }

    @GetMapping("/{provider}")
    public ResponseEntity<PaymentGatewayConfigResponse> getGateway(
            @PathVariable UUID workspaceId,
            @PathVariable String provider,
            @CurrentWorkspaceUser(requires = Permission.SETTINGS_VIEW) WorkspacePrincipal principal
    ) {
        return paymentSettingsService.getGateway(workspaceId, provider)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/{provider}")
    public ResponseEntity<PaymentGatewayConfigResponse> updateGateway(
            @PathVariable UUID workspaceId,
            @PathVariable String provider,
            @Valid @RequestBody UpdatePaymentGatewayConfigRequest request,
            @CurrentWorkspaceUser(requires = Permission.SETTINGS_EDIT) WorkspacePrincipal principal
    ) {
        return ResponseEntity.ok(paymentSettingsService.updateGateway(workspaceId, provider, request));
    }

    @PutMapping("/{provider}/primary")
    public ResponseEntity<Void> setPrimaryGateway(
            @PathVariable UUID workspaceId,
            @PathVariable String provider,
            @CurrentWorkspaceUser(requires = Permission.SETTINGS_EDIT) WorkspacePrincipal principal
    ) {
        paymentSettingsService.setPrimaryGateway(workspaceId, provider);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{provider}/toggle")
    public ResponseEntity<Void> toggleGateway(
            @PathVariable UUID workspaceId,
            @PathVariable String provider,
            @RequestBody Map<String, Boolean> body,
            @CurrentWorkspaceUser(requires = Permission.SETTINGS_EDIT) WorkspacePrincipal principal
    ) {
        boolean isEnabled = body.getOrDefault("isEnabled", true);
        paymentSettingsService.toggleGateway(workspaceId, provider, isEnabled);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{provider}")
    public ResponseEntity<Void> deleteGateway(
            @PathVariable UUID workspaceId,
            @PathVariable String provider,
            @CurrentWorkspaceUser(requires = Permission.SETTINGS_EDIT) WorkspacePrincipal principal
    ) {
        paymentSettingsService.deleteGateway(workspaceId, provider);
        return ResponseEntity.noContent().build();
    }

    // Legacy Razorpay endpoints for backwards compatibility
    @GetMapping("/razorpay")
    public ResponseEntity<WorkspacePaymentSettingsResponse> getRazorpaySettings(
            @PathVariable UUID workspaceId,
            @CurrentWorkspaceUser(requires = Permission.SETTINGS_VIEW) WorkspacePrincipal principal
    ) {
        WorkspacePaymentSettingsResponse response = paymentSettingsService.getSettings(workspaceId);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/razorpay")
    public ResponseEntity<WorkspacePaymentSettingsResponse> updateRazorpaySettings(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody UpdateWorkspacePaymentSettingsRequest request,
            @CurrentWorkspaceUser(requires = Permission.SETTINGS_EDIT) WorkspacePrincipal principal
    ) {
        return ResponseEntity.ok(paymentSettingsService.updateSettings(workspaceId, request));
    }
}
