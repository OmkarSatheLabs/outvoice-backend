package com.omkarsathe.outvoice.currency;

import java.util.UUID;

public record CurrencyDto(UUID id, String code, String name, String symbol, Short decimalPlaces) {}
