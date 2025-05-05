package com.abcmart.shoestore.domain;

import com.abcmart.shoestore.tool.CreditCardType;
import com.abcmart.shoestore.tool.PaymentType;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CardPayment extends Payment {

    private CreditCardType creditCardType;

    private CardPayment(CreditCardType creditCardType, BigDecimal paidAmount) {

        super(PaymentType.CREDIT_CARD, paidAmount);
        this.creditCardType = creditCardType;
    }

    public static CardPayment payInCreditCard(CreditCardType creditCardType, BigDecimal paidAmount) {

        return new CardPayment(creditCardType, paidAmount);
    }
}
