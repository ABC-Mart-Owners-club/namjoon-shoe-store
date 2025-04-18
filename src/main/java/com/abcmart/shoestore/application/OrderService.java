package com.abcmart.shoestore.application;

import com.abcmart.shoestore.application.request.CreateOrderRequest;
import com.abcmart.shoestore.domain.OrderPayment;
import com.abcmart.shoestore.dto.Order;
import com.abcmart.shoestore.entity.OrderDetailEntity;
import com.abcmart.shoestore.entity.OrderEntity;
import com.abcmart.shoestore.entity.ShoeEntity;
import com.abcmart.shoestore.repository.OrderRepository;
import com.abcmart.shoestore.repository.ShoeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final ShoeRepository shoeRepository;
    private final OrderRepository orderRepository;

    public Order createOrder(CreateOrderRequest request) {

        List<Long> shoeCodes = request.getOrderDetails().stream().map(CreateOrderRequest.CreateOrderDetailRequest::getShoeCode).toList();

        Map<Long, ShoeEntity> shoeEntityMap = shoeRepository.findAllByShoeCodes(shoeCodes).stream()
            .collect(Collectors.toMap(ShoeEntity::getShoeCode, Function.identity()));

        List<OrderDetailEntity> detailEntities = request.getOrderDetails().stream()
                .map(orderDetail -> OrderDetailEntity.create(orderDetail.getShoeCode(), orderDetail.getCount()))
                .toList();
        BigDecimal totalPrice = calculateTotalPrice(shoeEntityMap, request.getOrderDetails());
        OrderPayment orderPayment = OrderPayment.payInCash(totalPrice);
        OrderEntity orderEntity = OrderEntity.create(detailEntities, orderPayment);
        OrderEntity savedEntity = orderRepository.save(orderEntity);

        return Order.from(savedEntity);
    }

    private BigDecimal calculateTotalPrice(Map<Long, ShoeEntity> shoeEntityMap, List<CreateOrderRequest.CreateOrderDetailRequest> orderDetails) {

        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderRequest.CreateOrderDetailRequest orderDetail : orderDetails) {

            Long shoeCode = orderDetail.getShoeCode();
            ShoeEntity shoeEntity = shoeEntityMap.get(shoeCode);
            if (Objects.isNull(shoeEntity)) {
                continue;
            }

            BigDecimal shoesPrice = shoeEntity.getPrice().multiply(BigDecimal.valueOf(orderDetail.getCount()));
            total.add(shoesPrice);
        }

        return total;
    }
}
