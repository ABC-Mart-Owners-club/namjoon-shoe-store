package com.abcmart.shoestore.order.application.request;

import com.abcmart.shoestore.payment.domain.CreditCardType;
import com.abcmart.shoestore.payment.domain.PaymentType;
import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
    List<CreateOrderDetailRequest> orderDetails,
    List<CreatePaymentRequest> payments
) {

    public record CreateOrderDetailRequest(Long shoeCode, Long count) {}

    public record CreatePaymentRequest (
        PaymentType paymentType,
        CreditCardType creditCardType,
        BigDecimal paidAmount
    ) {}
}
