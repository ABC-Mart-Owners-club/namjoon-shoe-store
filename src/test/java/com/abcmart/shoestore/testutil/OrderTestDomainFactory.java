package com.abcmart.shoestore.testutil;

import com.abcmart.shoestore.domain.Order;
import com.abcmart.shoestore.domain.OrderDetail;
import com.abcmart.shoestore.domain.OrderPayment;
import com.abcmart.shoestore.tool.OrderStatus;
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

    public static Order createOrderBy(Long orderNo, List<OrderDetail> orderDetails, List<OrderPayment> orderPayments) {

        Map<String, OrderPayment> orderPaymentMap = orderPayments.stream()
            .collect(Collectors.toMap(OrderPayment::getId, Function.identity()));

        return fixtureMonkey.giveMeBuilder(Order.class)
            .set("orderNo", orderNo)
            .set("status", OrderStatus.NORMAL)
            .set("details", orderDetails)
            .set("orderPayments", orderPaymentMap)
            .sample();
    }

    public static OrderDetail createOrderDetail(Long shoeCode, Long count) {

        return fixtureMonkey.giveMeBuilder(OrderDetail.class)
            .set("orderStatus", OrderStatus.NORMAL)
            .set("shoeCode", shoeCode)
            .set("count", count)
            .sample();
    }
}
