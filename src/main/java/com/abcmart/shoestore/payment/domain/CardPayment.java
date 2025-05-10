package com.abcmart.shoestore.payment.domain;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CardPayment extends Payment {

    private CreditCardType creditCardType;

    //region Constructor
    private CardPayment(CreditCardType creditCardType, BigDecimal paidAmount) {

        super(PaymentType.CREDIT_CARD, paidAmount);
        this.creditCardType = creditCardType;
    }
    //endregion

    public static CardPayment payInCreditCard(CreditCardType creditCardType, BigDecimal paidAmount) {

        return new CardPayment(creditCardType, paidAmount);
    }
}
