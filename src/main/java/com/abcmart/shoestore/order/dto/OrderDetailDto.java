package com.abcmart.shoestore.order.dto;

import com.abcmart.shoestore.order.domain.OrderDetail;
import com.abcmart.shoestore.order.domain.OrderStatus;
import lombok.Getter;

@Getter
public class OrderDetailDto {

    private Long orderDetailNo;
    private OrderStatus orderStatus;
    private Long shoeCode;
    private Long count;

    private OrderDetailDto(Long orderDetailNo, OrderStatus orderStatus, Long shoeCode, Long count) {

        this.orderDetailNo = orderDetailNo;
        this.orderStatus = orderStatus;
        this.shoeCode = shoeCode;
        this.count = count;
    }

    public static OrderDetailDto from(OrderDetail orderDetail) {

        return new OrderDetailDto(
            orderDetail.getOrderDetailNo(),
            orderDetail.getOrderStatus(),
            orderDetail.getShoeCode(),
            orderDetail.getCount()
        );
    }
}
