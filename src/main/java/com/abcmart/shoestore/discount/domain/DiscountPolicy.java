package com.abcmart.shoestore.discount.domain;

import com.abcmart.shoestore.order.domain.Order;
import com.abcmart.shoestore.order.domain.discount.OrderDetailDiscount;
import java.util.List;

public interface DiscountPolicy {

    DiscountType getDiscountType();

    List<OrderDetailDiscount> apply(Order order);

}
