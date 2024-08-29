package com.example.payment_form.service.impl;

import com.example.payment_form.service.HttpClientService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;


@Slf4j
@Getter
@Service
public class HttpClientServiceImpl implements HttpClientService {

    @Value("${api.url}")
    private String apiUrl;

    @Value("${api.bearer.token}")
    private String bearerToken;

    @Value("${api.http-client.connect-time-out}")
    private int connectTimeOut;

    @Value("${api.http-client.request-time-out}")
    private int requestTimeOut;

    @Value("${api.http-client.max-retries}")
    private int maxRetries;


    @Override
    public Optional<HttpResponse<String>> sendPaymentRequest(String requestBody) {
        var httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(connectTimeOut))
                .build();

        var httpRequest = buildHttpRequest(requestBody);

        log.info("requestBody: {}", requestBody);

        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                log.info("Response body: {}", httpResponse.body());
                return Optional.of(httpResponse);
            } catch (IOException | InterruptedException e) {
                attempt++;
                log.error("Attempt {} failed: {}", attempt, e.getMessage());
                if (attempt >= maxRetries) {
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    private HttpRequest buildHttpRequest(String requestBody) {
        return HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("authorization", "Bearer " + bearerToken)
                .timeout(Duration.ofSeconds(requestTimeOut))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
    }
}
