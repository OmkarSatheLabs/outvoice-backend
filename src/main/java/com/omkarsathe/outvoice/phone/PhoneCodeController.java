package com.omkarsathe.outvoice.phone;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reference/phone-codes")
@RequiredArgsConstructor
public class PhoneCodeController {

    private final PhoneCodeService phoneCodeService;

    @GetMapping
    public List<PhoneCodeDto> getPhoneCodes() {
        return phoneCodeService.getAll();
    }
}
