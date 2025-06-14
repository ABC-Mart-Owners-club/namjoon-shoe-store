package com.abcmart.shoestore.order.application.request;

import com.abcmart.shoestore.payment.domain.CreditCardType;
import com.abcmart.shoestore.payment.domain.PaymentType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.springframework.util.StringUtils;

public record CreateOrderRequest(
    List<CreateOrderDetailRequest> orderDetails,
    String couponCode,
    List<CreatePaymentRequest> payments
) {

    public boolean existCoupon() {

        return Objects.nonNull(couponCode) && StringUtils.hasText(couponCode);
    }

    public record CreateOrderDetailRequest(Long shoeCode, Long count) {}

    public record CreatePaymentRequest (
        PaymentType paymentType,
        CreditCardType creditCardType,
        BigDecimal paidAmount
    ) {}
}
