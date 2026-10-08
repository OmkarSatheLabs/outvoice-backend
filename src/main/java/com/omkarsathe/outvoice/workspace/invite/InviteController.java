package com.omkarsathe.outvoice.workspace.invite;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class InviteController {

    @GetMapping("/api/workspace/invites")
    public List<String> getInvites() {
        return List.of();
    }

    @PostMapping("/api/workspaces/{workspaceId}/invites")
    public List<String> inviteMember(@PathVariable UUID workspaceId) {
        return List.of();
    }
}
