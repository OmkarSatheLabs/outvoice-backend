package com.omkarsathe.outvoice.workspace.customer;

import lombok.Getter;

@Getter
public enum SearchQueryTypeEnum {

    EMAIL("email"),
    MOBILE("mobile");

    private final String value;

    SearchQueryTypeEnum(String value) {
        this.value = value;
    }

}
