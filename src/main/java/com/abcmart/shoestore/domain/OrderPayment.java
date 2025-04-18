package com.abcmart.shoestore.domain;

import com.abcmart.shoestore.tool.PaymentType;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderPayment {

    private final PaymentType type;
    private final BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal cancelledAmount;

    private OrderPayment(PaymentType type, BigDecimal totalAmount) {

        this.type = type;
        this.totalAmount = totalAmount;
        this.paidAmount = totalAmount;
        this.cancelledAmount = BigDecimal.ZERO;
    }

    public static OrderPayment payInCash(BigDecimal amount) {

        return new OrderPayment(PaymentType.CASH, amount);
    }

    public OrderPayment partialCancel(BigDecimal cancelledAmount) {

        this.cancelledAmount = cancelledAmount;
        this.paidAmount = this.totalAmount.subtract(cancelledAmount);
        return this;
    }
}
