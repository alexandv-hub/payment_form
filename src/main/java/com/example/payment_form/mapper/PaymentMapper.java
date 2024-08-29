package com.example.payment_form.mapper;

import com.example.payment_form.dto.PaymentRequestDTO;
import com.example.payment_form.exception.PaymentProcessingException;
import com.example.payment_form.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = {CustomerMapper.class})
public interface PaymentMapper {

    @Mapping(source = "amount", target = "amount", qualifiedByName = "stringToBigDecimal")
    @Mapping(source = "customer", target = "customer")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Payment mapToPayment(PaymentRequestDTO paymentRequestDTO);

    @Named("stringToBigDecimal")
    default BigDecimal stringToBigDecimal(String value) {
        try {
            return new BigDecimal(value);
        }
        catch (NumberFormatException e) {
            throw new PaymentProcessingException("Invalid value '" + value + "'", e);
        }
    }
}
