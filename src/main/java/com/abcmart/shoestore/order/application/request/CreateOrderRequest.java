package com.abcmart.shoestore.order.application.request;

import com.abcmart.shoestore.payment.domain.CreditCardType;
import com.abcmart.shoestore.payment.domain.PaymentType;
import java.math.BigDecimal;
import lombok.Getter;

import java.util.List;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateOrderRequest {

    private final List<CreateOrderDetailRequest> orderDetails;
    private final List<CreatePaymentRequest> payments;

    @Getter
    @RequiredArgsConstructor
    public static class CreateOrderDetailRequest {

        private final Long shoeCode;
        private final Long count;
    }

    @Getter
    @RequiredArgsConstructor
    public static class CreatePaymentRequest {

        private final PaymentType paymentType;
        private final CreditCardType creditCardType;
        private final BigDecimal paidAmount;
    }
}
