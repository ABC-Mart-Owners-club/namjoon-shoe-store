package com.abcmart.shoestore.application;

import com.abcmart.shoestore.application.request.CreateOrderRequest;
import com.abcmart.shoestore.application.response.ShoeSaleCountResponse;
import com.abcmart.shoestore.application.response.ShoeSaleCountResponse.SoldShoe;
import com.abcmart.shoestore.domain.OrderPayment;
import com.abcmart.shoestore.dto.OrderDto;
import com.abcmart.shoestore.dto.ShoeDto;
import com.abcmart.shoestore.domain.OrderDetail;
import com.abcmart.shoestore.domain.Order;
import com.abcmart.shoestore.domain.Shoe;
import com.abcmart.shoestore.repository.OrderRepository;
import com.abcmart.shoestore.repository.ShoeRepository;
import java.math.BigDecimal;
import java.util.HashMap;
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

        List<Long> shoeCodes = request.getOrderDetails().stream()
            .map(CreateOrderRequest.CreateOrderDetailRequest::getShoeCode).toList();

        Map<Long, Shoe> shoeMap = shoeRepository.findAllByShoeCodes(shoeCodes).stream()
            .collect(Collectors.toMap(Shoe::getShoeCode, Function.identity()));

        List<OrderDetail> details = request.getOrderDetails().stream()
            .map(orderDetail -> OrderDetail.create(orderDetail.getShoeCode(),
                orderDetail.getCount()))
            .toList();
        BigDecimal totalPrice = calculateTotalPrice(shoeMap, request.getOrderDetails());
        OrderPayment orderPayment = OrderPayment.payInCash(totalPrice);
        Order order = Order.create(details, orderPayment);
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

    @Transactional(readOnly = true)
    public ShoeSaleCountResponse getShoeSaleCount() {

        List<OrderDetail> orderDetailList = orderRepository.findAllNormalStatusOrderDetails();

        List<Long> soldShoeCodes = orderDetailList.stream()
            .map(OrderDetail::getShoeCode).toList();
        Map<Long, Shoe> soldShoeMap = shoeRepository.findAllByShoeCodes(soldShoeCodes)
            .stream()
            .collect(Collectors.toMap(Shoe::getShoeCode, Function.identity()));

        HashMap<Long, SoldShoe> soldShoeHashMap = orderDetailList.stream()
            .filter(detail -> soldShoeMap.containsKey(detail.getShoeCode()))
            .collect(Collectors.toMap(
                OrderDetail::getShoeCode,
                detail -> {
                    ShoeDto shoeDto = ShoeDto.from(soldShoeMap.get(detail.getShoeCode()));
                    return SoldShoe.of(shoeDto, detail.getCount());
                },
                (existing, added) -> {
                    existing.updateSaleCountAndTotalPrice(added.getSaleCount());
                    return existing;
                },
                HashMap::new
            ));


        return ShoeSaleCountResponse.from(soldShoeHashMap.values().stream().toList());
    }
}
