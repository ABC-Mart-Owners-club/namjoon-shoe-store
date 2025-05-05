package com.abcmart.shoestore.dto;

import com.abcmart.shoestore.domain.Order;
import com.abcmart.shoestore.tool.OrderStatus;
import java.util.List;
import lombok.Getter;

@Getter
public class OrderDto {

    private final Long orderNo;
    private final OrderStatus status;
    private final List<OrderDetailDto> details;
    private final List<PaymentDto> payments;

    private OrderDto(Long orderNo, OrderStatus status, List<OrderDetailDto> details,
        List<PaymentDto> payments) {

        this.orderNo = orderNo;
        this.status = status;
        this.details = details;
        this.payments = payments;
    }

    public static OrderDto from(Order order) {

        return new OrderDto(
            order.getOrderNo(),
            order.getStatus(),
            order.getDetails().stream().map(OrderDetailDto::from).toList(),
            order.getPayments().values().stream().map(PaymentDto::from).toList()
        );
    }
}
