package com.abcmart.shoestore.domain;

import com.abcmart.shoestore.tool.PaymentType;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderPayment {

    private final PaymentType type;
    private final BigDecimal amount;

    private OrderPayment(PaymentType type, BigDecimal amount) {

        this.type = type;
        this.amount = amount;
    }

    public OrderPayment create(PaymentType type, BigDecimal amount) {

        return new OrderPayment(type, amount);
    }
}
