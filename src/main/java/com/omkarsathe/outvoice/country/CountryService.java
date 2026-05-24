package com.omkarsathe.outvoice.country;

import com.omkarsathe.outvoice.currency.Currency;
import com.omkarsathe.outvoice.currency.CurrencyDto;
import com.omkarsathe.outvoice.entity.CountryPhoneCodeRepository;
import com.omkarsathe.outvoice.phone.PhoneCodeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;
    private final CountryPhoneCodeRepository countryPhoneCodeRepository;

    @Transactional(readOnly = true)
    public List<CountryDto> getAllActive() {
        return countryRepository.findAllActiveWithCurrency()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public PhoneCodeDto getPrimaryPhoneCode(String isoCode2) {
        return countryPhoneCodeRepository.findPrimaryPhoneCodeByCountryIsoCode(isoCode2)
                .map(p -> new PhoneCodeDto(p.getId(), p.getCode()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No primary phone code found for country: " + isoCode2));
    }

    private CountryDto toDto(Country country) {
        CurrencyDto currencyDto = null;
        if (country.getDefaultCurrency() != null) {
            Currency c = country.getDefaultCurrency();
            currencyDto = new CurrencyDto(c.getId(), c.getCode(), c.getName(), c.getSymbol(), c.getDecimalPlaces());
        }
        return new CountryDto(country.getId(), country.getName(), country.getIsoCode2(), country.getIsoCode3(), currencyDto);
    }
}
