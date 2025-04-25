package com.abcmart.shoestore.domain;

import com.abcmart.shoestore.tool.CreditCardType;
import com.abcmart.shoestore.tool.PaymentType;
import jakarta.annotation.Nullable;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderPayment {

    @Id
    private Long id;

    @NotNull
    private PaymentType type;

    @Nullable
    private CreditCardType creditCardType;

    @NotNull
    private BigDecimal paidAmount;

    private OrderPayment(PaymentType type, @Nullable CreditCardType creditCardType, BigDecimal paidAmount) {

        this.type = type;
        this.creditCardType = creditCardType;
        this.paidAmount = paidAmount;
    }

    public static OrderPayment payInCash(BigDecimal paidAmount) {

        return new OrderPayment(PaymentType.CASH, null, paidAmount);
    }

    public static OrderPayment payInCreditCard(CreditCardType creditCardType, BigDecimal paidAmount) {

        return new OrderPayment(PaymentType.CREDIT_CARD, creditCardType, paidAmount);
    }

    public OrderPayment updatePaidAmountToZero() {

        this.paidAmount = BigDecimal.ZERO;
        return this;
    }

    public BigDecimal partialCancel(BigDecimal amountToCancel) {

        BigDecimal cancelAmount = this.paidAmount.min(amountToCancel);
        this.paidAmount = this.paidAmount.subtract(cancelAmount);
        return cancelAmount;
    }

    public boolean validateAvailableCancel(BigDecimal cancelledAmount) {

        return this.paidAmount.compareTo(cancelledAmount) >= 0;
    }
}
