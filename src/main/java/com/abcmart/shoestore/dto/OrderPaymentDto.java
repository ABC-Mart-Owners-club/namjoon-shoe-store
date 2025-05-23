package com.abcmart.shoestore.dto;

import com.abcmart.shoestore.domain.CardPayment;
import com.abcmart.shoestore.domain.OrderPayment;
import com.abcmart.shoestore.tool.CreditCardType;
import com.abcmart.shoestore.tool.PaymentType;
import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class OrderPaymentDto {

    private final PaymentType paymentType;

    @Nullable
    private final CreditCardType creditCardType;

    private final BigDecimal paidAmount;

    private OrderPaymentDto(PaymentType paymentType, @Nullable CreditCardType creditCardType,
        BigDecimal paidAmount) {

        this.paymentType = paymentType;
        this.creditCardType = creditCardType;
        this.paidAmount = paidAmount;
    }

    public static OrderPaymentDto from (OrderPayment orderPayment) {

        CreditCardType creditCardType = null;
        if (orderPayment instanceof CardPayment) {
            creditCardType = ((CardPayment) orderPayment).getCreditCardType();
        }

        return new OrderPaymentDto(
            orderPayment.getType(), creditCardType, orderPayment.getPaidAmount()
        );
    }
}
