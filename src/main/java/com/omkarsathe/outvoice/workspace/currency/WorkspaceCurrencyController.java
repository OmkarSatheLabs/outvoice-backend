package com.omkarsathe.outvoice.workspace.currency;

import ch.qos.logback.core.util.StringUtil;
import com.omkarsathe.outvoice.workspace.currency.dto.CurrencyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/currency")
@RequiredArgsConstructor
public class WorkspaceCurrencyController {

//    private final WorkspaceCurrencyService workspaceCurrencyService;
//
//    @GetMapping()
//    public CurrencyResponse get(@PathVariable String workspaceId, @AuthenticationPrincipal UserDetails userDetails) {
//        return workspaceCurrencyService.get(UUID.fromString(workspaceId), UUID.fromString(userDetails.getUsername()));
//    }
}
