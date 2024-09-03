package com.driver.exception;

public class CountryNotFoundException extends RuntimeException{
    public CountryNotFoundException(String countryNotFound) {
        super(countryNotFound);
    }
}
