package com.abcmart.shoestore.order.application;

import com.abcmart.shoestore.order.application.request.CreateOrderRequest;
import com.abcmart.shoestore.order.application.request.CreateOrderRequest.CreateOrderDetailRequest;
import com.abcmart.shoestore.order.domain.Order;
import com.abcmart.shoestore.order.domain.OrderDetail;
import com.abcmart.shoestore.order.repository.OrderRepository;
import com.abcmart.shoestore.shoe.domain.Shoe;
import com.abcmart.shoestore.shoe.repository.ShoeRepository;
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
    public Order findOrderByOrderNo(Long orderNo) {

        return orderRepository.findByOrderNo(orderNo)
            .orElseThrow(OrderService::orderNotFoundException);
    }

    @Transactional
    public Order createOrder(List<CreateOrderDetailRequest> orderDetailRequests) {

        // 주문을 원하는 신발의 가격 조회
        List<Long> shoeCodes = orderDetailRequests.stream()
            .map(CreateOrderRequest.CreateOrderDetailRequest::getShoeCode).toList();
        Map<Long, Shoe> shoeMap = shoeRepository.findAllByShoeCodes(shoeCodes).stream()
            .collect(Collectors.toMap(Shoe::getShoeCode, Function.identity()));

        // 주문 항목 생성
        List<OrderDetail> details = orderDetailRequests.stream()
            .filter(requestedDetail -> Objects.nonNull(shoeMap.get(requestedDetail.getShoeCode())))
            .map(requestedDetail ->
                OrderDetail.create(
                    requestedDetail.getShoeCode(),
                    shoeMap.get(requestedDetail.getShoeCode()).getPrice(),
                    requestedDetail.getCount()
                )
            )
            .toList();

        // 주문 생성 및 저장
        Order order = Order.create(details);
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderNo) {

        Order order = orderRepository.findByOrderNo(orderNo)
            .orElseThrow(OrderService::orderNotFoundException);
        order.totalCancel();
        return orderRepository.save(order);
    }

    @Transactional
    public Order partialCancel(Long orderNo, Long shoeCode, Long removeCount) {

        Order order = orderRepository.findByOrderNo(orderNo)
            .orElseThrow(OrderService::orderNotFoundException);
        order.partialCancel(shoeCode, removeCount);
        return orderRepository.save(order);
    }

    @Transactional
    public Order simpleUpdateOrder(Order order) {

        return orderRepository.save(order);
    }

    private static IllegalArgumentException orderNotFoundException() {

        return new IllegalArgumentException("Order not found");
    }
}
