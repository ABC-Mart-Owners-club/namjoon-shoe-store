package com.abcmart.shoestore.testutil;

import com.abcmart.shoestore.order.domain.Order;
import com.abcmart.shoestore.order.domain.OrderDetail;
import com.abcmart.shoestore.order.domain.OrderStatus;
import com.abcmart.shoestore.payment.domain.Payment;
import com.abcmart.shoestore.shoe.domain.Shoe;
import com.abcmart.shoestore.utils.ShoeProductCodeUtils;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OrderTestDomainFactory extends TestDomainFactory<Order> {

    public static Order createOrderByOrderNo(Long orderNo) {

        return fixtureMonkey.giveMeBuilder(Order.class)
            .set("orderNo", 1L)
            .set("status", OrderStatus.NORMAL)
            .sample();
    }

    public static Order createOrderBy(Long orderNo, List<OrderDetail> orderDetails, List<Payment> payments) {

        Map<String, Payment> paymentMap = payments.stream()
            .collect(Collectors.toMap(Payment::getId, Function.identity()));

        return fixtureMonkey.giveMeBuilder(Order.class)
            .set("orderNo", orderNo)
            .set("status", OrderStatus.NORMAL)
            .set("details", orderDetails)
            .set("payments", paymentMap)
            .sample();
    }

    public static OrderDetail createOrderDetail(Shoe shoe, LocalDate stockedDate, Long count) {

        return fixtureMonkey.giveMeBuilder(OrderDetail.class)
            .set("orderDetailStatus", OrderStatus.NORMAL)
            .set("shoeProductCode", ShoeProductCodeUtils.generate(shoe.getShoeCode(), stockedDate))
            .set("unitPrice", shoe.getPrice())
            .set("count", count)
            .set("discounts", new ArrayList<>())
            .sample();
    }
}
