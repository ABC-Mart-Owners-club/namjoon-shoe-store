package com.abcmart.shoestore.dto;

import com.abcmart.shoestore.domain.OrderPayment;
import com.abcmart.shoestore.domain.Order;
import com.abcmart.shoestore.tool.OrderStatus;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderDto {

    private Long orderNo;
    private OrderStatus status;
    private List<OrderDetailDto> details;
    private OrderPayment orderPayment;

    private OrderDto(Long orderNo, OrderStatus status, List<OrderDetailDto> details, OrderPayment orderPayment) {

        this.orderNo = orderNo;
        this.status = status;
        this.details = details;
        this.orderPayment = orderPayment;
    }

    public static OrderDto from(Order order) {

        return new OrderDto(
            order.getOrderNo(),
            order.getStatus(),
            order.getDetails().stream().map(OrderDetailDto::from).toList(),
            order.getOrderPayment()
        );
    }
}
