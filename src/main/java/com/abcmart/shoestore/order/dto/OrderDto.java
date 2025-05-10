package com.abcmart.shoestore.order.dto;

import com.abcmart.shoestore.order.domain.Order;
import com.abcmart.shoestore.order.domain.OrderStatus;
import com.abcmart.shoestore.payment.domain.Payment;
import com.abcmart.shoestore.payment.dto.PaymentDto;
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

    public static OrderDto from(Order order, List<Payment> payments) {

        return new OrderDto(
            order.getOrderNo(),
            order.getStatus(),
            order.getDetails().stream().map(OrderDetailDto::from).toList(),
            payments.stream().map(PaymentDto::from).toList()
        );
    }
}
