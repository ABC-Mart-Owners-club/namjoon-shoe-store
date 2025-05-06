package com.abcmart.shoestore.order.application;

import com.abcmart.shoestore.order.application.request.CreateOrderRequest;
import com.abcmart.shoestore.order.domain.Order;
import com.abcmart.shoestore.order.domain.OrderDetail;
import com.abcmart.shoestore.order.dto.OrderDto;
import com.abcmart.shoestore.order.repository.OrderRepository;
import com.abcmart.shoestore.payment.domain.CardPayment;
import com.abcmart.shoestore.payment.domain.CashPayment;
import com.abcmart.shoestore.payment.domain.Payment;
import com.abcmart.shoestore.shoe.domain.Shoe;
import com.abcmart.shoestore.shoe.repository.ShoeRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ShoeRepository shoeRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {

        // 주문을 원하는 신발의 가격 조회
        List<Long> shoeCodes = request.getOrderDetails().stream()
            .map(CreateOrderRequest.CreateOrderDetailRequest::getShoeCode).toList();
        Map<Long, Shoe> shoeMap = shoeRepository.findAllByShoeCodes(shoeCodes).stream()
            .collect(Collectors.toMap(Shoe::getShoeCode, Function.identity()));

        // 주문 항목 생성
        List<OrderDetail> details = request.getOrderDetails().stream()
            .filter(requestedDetail -> Objects.nonNull(shoeMap.get(requestedDetail.getShoeCode())))
            .map(requestedDetail ->
                OrderDetail.create(
                    requestedDetail.getShoeCode(),
                    shoeMap.get(requestedDetail.getShoeCode()).getPrice(),
                    requestedDetail.getCount()
                )
            )
            .toList();

        // 전체 가격 계산
//        BigDecimal totalPrice = calculateTotalPrice(shoeMap, request.getOrderDetails());

        // 결제 방식 생성
        List<Payment> payments = request.getPayments().stream()
            .map(paymentRequest -> {
                if (paymentRequest.getPaymentType().isCash()) {
                    return CashPayment.payInCash(paymentRequest.getPaidAmount());
                }
                return CardPayment.payInCreditCard(paymentRequest.getCreditCardType(),
                    paymentRequest.getPaidAmount());
            })
            .toList();

        // 주문 생성 및 저장
        Order order = Order.create(details, payments);
        Order savedOrder = orderRepository.save(order);

        return OrderDto.from(savedOrder);
    }

    private BigDecimal calculateTotalPrice(Map<Long, Shoe> shoeMap,
        List<CreateOrderRequest.CreateOrderDetailRequest> orderDetails) {

        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderRequest.CreateOrderDetailRequest orderDetail : orderDetails) {

            Long shoeCode = orderDetail.getShoeCode();
            Shoe shoe = shoeMap.get(shoeCode);
            if (Objects.isNull(shoe)) {
                continue;
            }

            BigDecimal shoesPrice = shoe.getPrice()
                .multiply(BigDecimal.valueOf(orderDetail.getCount()));
            total = total.add(shoesPrice);
        }

        return total;
    }

    @Transactional
    public OrderDto cancelOrder(Long orderNo) {

        Order targetOrder = orderRepository.findByOrderNo(orderNo);
        targetOrder.totalCancel();
        Order savedOrder = orderRepository.save(targetOrder);

        return OrderDto.from(savedOrder);
    }

    @Transactional
    public OrderDto partialCancel(Long orderNo, Long shoeCode, Long removeCount) {

        Shoe shoe = shoeRepository.findByShoeCode(shoeCode);

        Order order = orderRepository.findByOrderNo(orderNo);
        order.partialCancel(shoe, removeCount);
        Order savedOrder = orderRepository.save(order);

        return OrderDto.from(savedOrder);
    }
}
