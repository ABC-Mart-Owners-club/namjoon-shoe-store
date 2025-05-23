package com.abcmart.shoestore.application;

import com.abcmart.shoestore.application.request.CreateOrderRequest;
import com.abcmart.shoestore.domain.CardPayment;
import com.abcmart.shoestore.domain.CashPayment;
import com.abcmart.shoestore.domain.Order;
import com.abcmart.shoestore.domain.OrderDetail;
import com.abcmart.shoestore.domain.OrderPayment;
import com.abcmart.shoestore.domain.Shoe;
import com.abcmart.shoestore.dto.OrderDto;
import com.abcmart.shoestore.repository.OrderRepository;
import com.abcmart.shoestore.repository.ShoeRepository;
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

        // 주문 항목 생성
        List<OrderDetail> details = request.getOrderDetails().stream()
            .map(orderDetail -> OrderDetail.create(orderDetail.getShoeCode(),
                orderDetail.getCount()))
            .toList();

        // 신발들을 조회하여 전체 가격 계산
        List<Long> shoeCodes = request.getOrderDetails().stream()
            .map(CreateOrderRequest.CreateOrderDetailRequest::getShoeCode).toList();
        Map<Long, Shoe> shoeMap = shoeRepository.findAllByShoeCodes(shoeCodes).stream()
            .collect(Collectors.toMap(Shoe::getShoeCode, Function.identity()));

        BigDecimal totalPrice = calculateTotalPrice(shoeMap, request.getOrderDetails());

        // 결제 방식 생성
        List<OrderPayment> orderPayments = request.getPayments().stream()
            .map(paymentRequest -> {
                if (paymentRequest.getPaymentType().isCash()) {
                    return CashPayment.payInCash(paymentRequest.getPaidAmount());
                }
                return CardPayment.payInCreditCard(paymentRequest.getCreditCardType(),
                    paymentRequest.getPaidAmount());
            })
            .toList();

        // 주문 생성 및 저장
        Order order = Order.create(details, totalPrice, orderPayments);
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
