//package com.omkarsathe.outvoice.country;
//
//import com.omkarsathe.outvoice.phone.PhoneCodeDto;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/reference/countries")
//@RequiredArgsConstructor
//public class CountryController {
//
//    private final CountryService countryService;
//
//    @GetMapping
//    public List<CountryDto> getCountries(HttpServletRequest request) {
//        String ip = getClientIp(request);
//        return countryService.getAllActive(ip);
//    }
//
//    private String getClientIp(HttpServletRequest request) {
//        String xfHeader = request.getHeader("X-Forwarded-For");
//
//        if (xfHeader == null || xfHeader.isBlank()) {
//            return request.getRemoteAddr();
//        }
//
//        return xfHeader.split(",")[0].trim();
//    }
//
////    @GetMapping("/{isoCode2}/primary-phone-code")
////    public PhoneCodeDto getPrimaryPhoneCode(@PathVariable String isoCode2) {
////        return countryService.getPrimaryPhoneCode(isoCode2.toUpperCase());
////    }
//}
