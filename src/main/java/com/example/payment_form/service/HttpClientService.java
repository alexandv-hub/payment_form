package com.example.payment_form.service;

import java.net.http.HttpResponse;
import java.util.Optional;

public interface HttpClientService {

    Optional<HttpResponse<String>> sendPaymentRequest(String requestBody);

}
