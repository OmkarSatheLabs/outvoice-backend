//package com.omkarsathe.outvoice.country;
//
//import com.omkarsathe.outvoice.currency.CurrencyEntity;
//import com.omkarsathe.outvoice.currency.CurrencyDto;
//import com.omkarsathe.outvoice.entity.CountryPhoneCodeRepository;
//import com.omkarsathe.outvoice.phone.PhoneCodeDto;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class CountryService {
//
//    private final CountryRepository countryRepository;
//    private final CountryPhoneCodeRepository countryPhoneCodeRepository;
//    private final GeoIpService geoIpService;
//
//    @Transactional(readOnly = true)
//    public List<CountryDto> getAllActive(String ip) {
//        String countryCode = geoIpService.getCountryCode(ip);
//
//        return countryRepository
//                .findAllActiveWithCurrencyOrderByCountryFirst(countryCode)
//                .stream()
//                .map(this::toDto)
//                .toList();
//    }
//
//    public Country findById(UUID countryId) {
//        return countryRepository.findById(countryId).orElseThrow(() -> new RuntimeException("Country with UUID " + countryId + " not found"));
//    }
//
////    @Transactional(readOnly = true)
////    public PhoneCodeDto getPrimaryPhoneCode(String isoCode2) {
////        return countryPhoneCodeRepository.findPrimaryPhoneCodeByCountryIsoCode(isoCode2)
////                .map(p -> new PhoneCodeDto(p.getId(), p.getCode()))
////                .orElseThrow(() -> new ResponseStatusException(
////                        HttpStatus.NOT_FOUND, "No primary phone code found for country: " + isoCode2));
////    }
//
//    @Transactional(readOnly = true)
//    private CountryDto toDto(Country country) {
//        CurrencyDto currencyDto = null;
//        if (country.getDefaultCurrency() != null) {
//            CurrencyEntity c = country.getDefaultCurrency();
//            currencyDto = new CurrencyDto(c.getId(), c.getCode(), c.getName(), c.getSymbol(), c.getDecimalPlaces());
//        }
//
//        PhoneCodeDto phoneCodeDto =
//                countryPhoneCodeRepository
//                        .findPrimaryPhoneCodeByCountryIsoCode(country.getIsoCode2())
//                        .map(pc -> new PhoneCodeDto(pc.getId(), pc.getCode()))
//                        .orElse(null);
//
//        return new CountryDto(country.getId(), country.getName(), country.getIsoCode2(), country.getIsoCode3(), currencyDto, phoneCodeDto);
//    }
//}
