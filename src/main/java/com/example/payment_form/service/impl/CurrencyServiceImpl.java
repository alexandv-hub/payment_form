package com.example.payment_form.service.impl;

import com.example.payment_form.service.CurrencyService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    private static final List<Currency> POPULAR_CURRENCIES = Arrays.asList(
            Currency.getInstance("USD"), // US Dollar
            Currency.getInstance("EUR"), // Euro
            Currency.getInstance("GBP"), // British Pound
            Currency.getInstance("JPY"), // Japanese Yen
            Currency.getInstance("AUD"), // Australian Dollar
            Currency.getInstance("CAD"), // Canadian Dollar
            Currency.getInstance("CHF"), // Swiss Franc
            Currency.getInstance("CNY"), // Chinese Yuan
            Currency.getInstance("SEK"), // Swedish Krona
            Currency.getInstance("NZD")  // New Zealand Dollar
    );

    @Override
    public List<String> getCurrencies() {
        return POPULAR_CURRENCIES.stream()
                .map(Currency::getCurrencyCode)
                .collect(Collectors.toList());
    }
}
