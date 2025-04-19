package com.abcmart.shoestore.application;

import com.abcmart.shoestore.application.request.CreateOrderRequest;
import com.abcmart.shoestore.application.response.ShoeSaleCountResponse;
import com.abcmart.shoestore.domain.OrderPayment;
import com.abcmart.shoestore.dto.Order;
import com.abcmart.shoestore.dto.Shoe;
import com.abcmart.shoestore.entity.OrderDetailEntity;
import com.abcmart.shoestore.entity.OrderEntity;
import com.abcmart.shoestore.entity.ShoeEntity;
import com.abcmart.shoestore.repository.OrderRepository;
import com.abcmart.shoestore.repository.ShoeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
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
            total = total.add(shoesPrice);
        }

        return total;
    }

    @Transactional
    public Order cancelOrder(Long orderNo) {

        OrderEntity targetOrderEntity = orderRepository.findByOrderNo(orderNo);
        targetOrderEntity.totalCancel();
        OrderEntity savedEntity = orderRepository.save(targetOrderEntity);

        return Order.from(savedEntity);
    }

    @Transactional
    public Order partialCancel(Long orderNo, Long shoeCode, Long removeCount) {

        ShoeEntity shoeEntity = shoeRepository.findByShoeCode(shoeCode);

        OrderEntity orderEntity = orderRepository.findByOrderNo(orderNo);
        orderEntity.partialCancel(shoeEntity, removeCount);
        OrderEntity savedEntity = orderRepository.save(orderEntity);

        return Order.from(savedEntity);
    }

    @Transactional
    public ShoeSaleCountResponse getShoeSaleCount() {

        List<OrderDetailEntity> orderDetailEntityList = orderRepository.findAllNormalStatusOrderDetails();

        List<Long> soldShoeCodes = orderDetailEntityList.stream().map(OrderDetailEntity::getShoeCode).toList();
        Map<Long, ShoeEntity> soldShoeEntityMap = shoeRepository.findAllByShoeCodes(soldShoeCodes).stream()
                .collect(Collectors.toMap(ShoeEntity::getShoeCode, Function.identity()));

        List<ShoeSaleCountResponse.SoldShoe> soldShoeList = orderDetailEntityList.stream()
            .map(detailEntity -> {
                ShoeEntity shoeEntity = soldShoeEntityMap.get(detailEntity.getShoeCode());
                if (Objects.isNull(shoeEntity)) {
                    return null;
                }

                Shoe shoe = Shoe.from(shoeEntity);
                Long saleCount = detailEntity.getCount();

                return ShoeSaleCountResponse.SoldShoe.of(shoe, saleCount);
            })
            .filter(Objects::nonNull)
            .toList();

        return ShoeSaleCountResponse.from(soldShoeList);
    }
}
