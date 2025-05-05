package com.abcmart.shoestore.dto;

import com.abcmart.shoestore.domain.CardPayment;
import com.abcmart.shoestore.domain.Payment;
import com.abcmart.shoestore.tool.CreditCardType;
import com.abcmart.shoestore.tool.PaymentType;
import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class PaymentDto {

    private final PaymentType paymentType;

    @Nullable
    private final CreditCardType creditCardType;

    private final BigDecimal paidAmount;

    private PaymentDto(PaymentType paymentType, @Nullable CreditCardType creditCardType,
        BigDecimal paidAmount) {

        this.paymentType = paymentType;
        this.creditCardType = creditCardType;
        this.paidAmount = paidAmount;
    }

    public static PaymentDto from (Payment payment) {

        CreditCardType creditCardType = null;
        if (payment instanceof CardPayment) {
            creditCardType = ((CardPayment) payment).getCreditCardType();
        }

        return new PaymentDto(
            payment.getType(), creditCardType, payment.getPaidAmount()
        );
    }
}
