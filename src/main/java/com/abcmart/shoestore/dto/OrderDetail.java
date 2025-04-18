package com.abcmart.shoestore.dto;

import com.abcmart.shoestore.entity.OrderDetailEntity;
import com.abcmart.shoestore.tool.OrderStatus;
import lombok.Getter;

@Getter
public class OrderDetail {

    private Long orderDetailNo;
    private OrderStatus orderStatus;
    private Long shoeCode;
    private Long count;

    private OrderDetail(Long orderDetailNo, OrderStatus orderStatus, Long shoeCode, Long count) {

        this.orderDetailNo = orderDetailNo;
        this.orderStatus = orderStatus;
        this.shoeCode = shoeCode;
        this.count = count;
    }

    public static OrderDetail from(OrderDetailEntity orderDetailEntity) {

        return new OrderDetail(
            orderDetailEntity.getOrderDetailNo(),
            orderDetailEntity.getOrderStatus(),
            orderDetailEntity.getShoeCode(),
            orderDetailEntity.getCount()
        );
    }
}
