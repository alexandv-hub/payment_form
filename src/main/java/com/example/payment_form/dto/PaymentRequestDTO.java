package com.example.payment_form.dto;

import com.example.payment_form.model.PaymentMethod;
import com.example.payment_form.model.PaymentType;
import com.example.payment_form.validation.ValidAmount;
import com.example.payment_form.validation.ValidCurrency;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PaymentRequestDTO {

    private PaymentType paymentType;
    private PaymentMethod paymentMethod;

    @ValidAmount
    private String amount;

    @ValidCurrency
    private String currency;

    private CustomerDTO customer;

}
