package com.omkarsathe.outvoice.currency;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final CurrencyRepository currencyRepository;

    @Transactional(readOnly = true)
    public List<CurrencyDto> getAllActive() {
        return currencyRepository.findByIsActiveTrueOrderByCodeAsc()
                .stream()
                .map(c -> new CurrencyDto(c.getId(), c.getCode(), c.getName(), c.getSymbol(), c.getDecimalPlaces()))
                .toList();
    }
}
