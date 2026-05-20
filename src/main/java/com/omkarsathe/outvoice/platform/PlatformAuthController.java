package com.omkarsathe.outvoice.platform;

import com.omkarsathe.outvoice.platform.dto.PlatformLoginRequest;
import com.omkarsathe.outvoice.platform.dto.PlatformLoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/platform/auth")
@RequiredArgsConstructor
public class PlatformAuthController {

    private final PlatformAuthService platformAuthService;

    @PostMapping("/login")
    public PlatformLoginResponse login(@Valid @RequestBody PlatformLoginRequest request) {
        return platformAuthService.login(request);
    }
}
