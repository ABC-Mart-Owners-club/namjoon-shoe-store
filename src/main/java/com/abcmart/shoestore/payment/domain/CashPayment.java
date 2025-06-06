package com.abcmart.shoestore.payment.domain;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CashPayment extends Payment {

    //region Constructor
    private CashPayment(BigDecimal paidAmount) {

        super(PaymentType.CASH, paidAmount);
    }
    //endregion

    public static CashPayment payInCash(BigDecimal paidAmount) {

        return new CashPayment(paidAmount);
    }
}
