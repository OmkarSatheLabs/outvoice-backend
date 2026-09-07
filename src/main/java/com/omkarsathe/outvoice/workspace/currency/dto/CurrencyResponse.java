package com.omkarsathe.outvoice.workspace.currency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class CurrencyResponse {
    private UUID id;
    private final String code;
    private final String name;
    private final String symbol;
    private Short decimalPlaces;
}
