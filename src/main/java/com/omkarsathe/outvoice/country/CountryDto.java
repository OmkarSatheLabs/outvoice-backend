package com.omkarsathe.outvoice.country;

import com.omkarsathe.outvoice.currency.CurrencyDto;
import com.omkarsathe.outvoice.phone.PhoneCodeDto;

import java.util.UUID;

public record CountryDto(UUID id, String name, String isoCode2, String isoCode3, CurrencyDto defaultCurrency, PhoneCodeDto defaultPhoneCode) {}
