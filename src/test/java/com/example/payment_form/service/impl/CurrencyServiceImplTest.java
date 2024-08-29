package com.example.payment_form.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyServiceImplTest {

    private CurrencyServiceImpl currencyService;

    @BeforeEach
    void setUp() {
        currencyService = new CurrencyServiceImpl();
    }

    @Test
    void whenGetCurrencies_thenReturnListOfPopularCurrencies() {
        List<String> currencies = currencyService.getCurrencies();

        assertEquals(10, currencies.size(), "Expected 10 popular currencies");

        assertTrue(currencies.contains("USD"), "USD should be present");
        assertTrue(currencies.contains("EUR"), "EUR should be present");
        assertTrue(currencies.contains("GBP"), "GBP should be present");
    }

    @Test
    void whenGetCurrencies_thenReturnCurrenciesInCorrectOrder() {
        List<String> currencies = currencyService.getCurrencies();

        List<String> expectedCurrencies = List.of(
                "USD", "EUR", "GBP", "JPY", "AUD",
                "CAD", "CHF", "CNY", "SEK", "NZD"
        );

        assertEquals(expectedCurrencies, currencies, "Currencies should be in the expected order");
    }

    @Test
    void whenGetCurrencies_thenNoDuplicateCurrencies() {
        List<String> currencies = currencyService.getCurrencies();

        assertEquals(10, currencies.size(), "Expected 10 popular currencies");
        assertEquals(currencies.stream().distinct().count(), currencies.size(), "There should be no duplicate currencies");
    }

    @Test
    void whenGetCurrencies_thenReturnCorrectCurrencyNames() {
        List<String> currencies = currencyService.getCurrencies();

        assertTrue(currencies.stream().allMatch(code -> Currency.getInstance(code) != null), "All currency codes should be valid ISO 4217 codes");
    }
}
