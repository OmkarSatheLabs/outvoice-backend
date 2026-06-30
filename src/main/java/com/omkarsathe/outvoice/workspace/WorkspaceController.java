package com.omkarsathe.outvoice.workspace;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    @GetMapping
    public String index() {
        return "Hello World!";
    }

    @GetMapping("/invites")
    public String index2() {
        return "Hello World!";
    }
}
