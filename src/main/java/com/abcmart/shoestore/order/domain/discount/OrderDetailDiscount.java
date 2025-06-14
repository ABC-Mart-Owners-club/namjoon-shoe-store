package com.abcmart.shoestore.order.domain.discount;

import com.abcmart.shoestore.discount.domain.DiscountType;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class OrderDetailDiscount {

    private final Long orderDetailNo;
    private final DiscountType discountType;
    private final BigDecimal discountedAmount;

    public OrderDetailDiscount(Long orderDetailNo, DiscountType discountType,
        BigDecimal discountedAmount) {
        this.orderDetailNo = orderDetailNo;
        this.discountType = discountType;
        this.discountedAmount = discountedAmount;
    }

    public static OrderDetailDiscount of(Long orderDetailNo, DiscountType type, BigDecimal amount) {
        return new OrderDetailDiscount(orderDetailNo, type, amount);
    }
}