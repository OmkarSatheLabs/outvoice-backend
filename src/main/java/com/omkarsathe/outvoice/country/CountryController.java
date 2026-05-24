package com.omkarsathe.outvoice.country;

import com.omkarsathe.outvoice.phone.PhoneCodeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reference/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    @GetMapping
    public List<CountryDto> getCountries() {
        return countryService.getAllActive();
    }

    @GetMapping("/{isoCode2}/primary-phone-code")
    public PhoneCodeDto getPrimaryPhoneCode(@PathVariable String isoCode2) {
        return countryService.getPrimaryPhoneCode(isoCode2.toUpperCase());
    }
}
