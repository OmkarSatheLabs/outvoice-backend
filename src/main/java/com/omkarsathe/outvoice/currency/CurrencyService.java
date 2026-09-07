//package com.omkarsathe.outvoice.currency;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class CurrencyService {
//
//    private final CurrencyRepository currencyRepository;
//
//    public CurrencyEntity findById(UUID currencyId) {
//        return currencyRepository.findById(currencyId).orElseThrow(() -> new RuntimeException("CurrencyEntity with UUID " + currencyId + " not found"));
//    }
//
//    @Transactional(readOnly = true)
//    public List<CurrencyDto> getAllActive() {
//        return currencyRepository.findByIsActiveTrueOrderByCodeAsc()
//                .stream()
//                .map(c -> new CurrencyDto(c.getId(), c.getCode(), c.getName(), c.getSymbol(), c.getDecimalPlaces()))
//                .toList();
//    }
//}
