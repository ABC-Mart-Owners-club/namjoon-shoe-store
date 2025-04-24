package com.abcmart.shoestore.domain;

import com.abcmart.shoestore.tool.PaymentType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderPayment {

    @NotNull
    private PaymentType type;

    @NotNull
    private BigDecimal totalAmount;

    @NotNull
    private BigDecimal paidAmount;

    @NotNull
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
