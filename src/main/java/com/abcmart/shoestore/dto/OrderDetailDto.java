package com.abcmart.shoestore.dto;

import com.abcmart.shoestore.domain.OrderDetail;
import com.abcmart.shoestore.tool.OrderStatus;
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
