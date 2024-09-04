package com.driver.model;

import java.util.*;
import java.lang.Enum;

public enum CountryName {
    IND("001"),
    USA("002"),
    AUS("003"),
    CHI("004"),
    JPN("005");

    private final String code;

    private CountryName(String s) {
        code = s;
    }

    public String toCode() {
        return this.code;
    }

    public static Optional<CountryName> byNameIgnoreCase(String givenName) {
        return Arrays.stream(values()).filter(it -> it.name().equalsIgnoreCase(givenName)).findAny();

    }

    public static Optional<CountryName> byFullNameIgnoreCase(String givenFullName) {
        return Arrays.stream(values()).filter(it -> it.code.equalsIgnoreCase(givenFullName)).findAny();
    }
}
