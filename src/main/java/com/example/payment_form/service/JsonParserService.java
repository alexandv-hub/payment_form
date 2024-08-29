package com.example.payment_form.service;

import org.springframework.stereotype.Service;

@Service
public interface JsonParserService {

    String extractRedirectUrl(String responseBody);

    String extractResponseId(String responseBody);
}
