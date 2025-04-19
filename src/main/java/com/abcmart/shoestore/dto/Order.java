package com.abcmart.shoestore.dto;

import com.abcmart.shoestore.domain.OrderPayment;
import com.abcmart.shoestore.entity.OrderEntity;
import com.abcmart.shoestore.tool.OrderStatus;
import lombok.Getter;

import java.util.List;

@Getter
public class Order {

    private Long orderNo;
    private OrderStatus status;
    private List<OrderDetail> details;
    private OrderPayment orderPayment;

    private Order(Long orderNo, OrderStatus status, List<OrderDetail> details, OrderPayment orderPayment) {

        this.orderNo = orderNo;
        this.status = status;
        this.details = details;
        this.orderPayment = orderPayment;
    }

    public static Order from(OrderEntity orderEntity) {

        return new Order(
            orderEntity.getOrderNo(),
            orderEntity.getStatus(),
            orderEntity.getDetails().stream().map(OrderDetail::from).toList(),
            orderEntity.getOrderPayment()
        );
    }
}
