package com.abcmart.shoestore.testutil;

import com.abcmart.shoestore.payment.domain.CardPayment;
import com.abcmart.shoestore.payment.domain.CashPayment;
import com.abcmart.shoestore.payment.domain.Payment;
import com.abcmart.shoestore.payment.domain.PaymentType;
import java.math.BigDecimal;
import java.util.UUID;
import net.jqwik.api.Arbitraries;

public class PaymentTestDomainFactory extends TestDomainFactory<Payment> {

    public static Payment createCash() {

        return fixtureMonkey.giveMeBuilder(CashPayment.class)
            .set("id", UUID.randomUUID().toString())
            .set("type", PaymentType.CASH)
            .set("paidAmount", Arbitraries.bigDecimals().greaterThan(BigDecimal.ZERO))
            .sample();
    }

    public static Payment createCreditCard() {

        return fixtureMonkey.giveMeBuilder(CardPayment.class)
            .set("id", UUID.randomUUID().toString())
            .set("type", PaymentType.CREDIT_CARD)
            .setNotNull("creditCardType")
            .set("paidAmount", Arbitraries.bigDecimals().greaterThan(BigDecimal.ZERO))
            .sample();
    }
}
