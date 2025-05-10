package com.abcmart.shoestore.order.application;

import com.abcmart.shoestore.order.application.request.CreateOrderRequest;
import com.abcmart.shoestore.order.domain.Order;
import com.abcmart.shoestore.order.dto.OrderDto;
import com.abcmart.shoestore.payment.application.PaymentService;
import com.abcmart.shoestore.payment.domain.Payment;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderFacadeService {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {

        Order order = orderService.createOrder(request.getOrderDetails());
        List<Payment> payments = paymentService.createPayment(request.getPayments());
        order.updatePaymentIds(payments.stream().map(Payment::getId).toList());
        Order updatedOrder = orderService.simpleUpdateOrder(order);

        return OrderDto.from(updatedOrder, payments);
    }

    @Transactional
    public OrderDto cancelOrder(Long orderNo) {

        Order order = orderService.cancelOrder(orderNo);
        List<Payment> payments = paymentService.cancelAllByPaymentIds(order.getPaymentIds());
        return OrderDto.from(order, payments);
    }

    @Transactional
    public OrderDto partialCancel(Long orderNo, Long shoeCode, Long removeCount) {

        Order storedOrder = orderService.findOrderByOrderNo(orderNo);
        Order partialCancelledOrder = orderService.partialCancel(orderNo, shoeCode, removeCount);

        BigDecimal cancelledAmount = storedOrder.getCurrentTotalAmount()
            .subtract(partialCancelledOrder.getCurrentTotalAmount());
        List<Payment> cancelledPayments = paymentService.partialCancel(
            partialCancelledOrder.getPaymentIds(), cancelledAmount
        );
        return OrderDto.from(partialCancelledOrder, cancelledPayments);
    }
}
