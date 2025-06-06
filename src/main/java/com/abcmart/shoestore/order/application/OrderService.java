package com.abcmart.shoestore.order.application;

import com.abcmart.shoestore.inventory.domain.Inventory;
import com.abcmart.shoestore.inventory.repository.InventoryRepository;
import com.abcmart.shoestore.order.application.request.CreateOrderRequest;
import com.abcmart.shoestore.order.application.request.CreateOrderRequest.CreateOrderDetailRequest;
import com.abcmart.shoestore.order.domain.Order;
import com.abcmart.shoestore.order.domain.OrderDetail;
import com.abcmart.shoestore.order.repository.OrderRepository;
import com.abcmart.shoestore.shoe.domain.Shoe;
import com.abcmart.shoestore.shoe.repository.ShoeRepository;
import java.util.ArrayList;
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
    private final InventoryRepository inventoryRepository;

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
        shoeCodes = shoeMap.keySet().stream().toList();

        Map<Long, Inventory> inventoryMap = inventoryRepository.findAllByShoeCodes(shoeCodes)
            .stream()
            .collect(Collectors.toMap(Inventory::getShoeCode, Function.identity()));

        // 주문 항목 생성
        ArrayList<Inventory> updatedInventories = new ArrayList<>();
        ArrayList<OrderDetail> details = new ArrayList<>();
        for (CreateOrderDetailRequest requestedDetail : orderDetailRequests) {

            // 가게에 안파는 신발 무시
            if (Objects.isNull(shoeMap.get(requestedDetail.getShoeCode()))) continue;

            // 재고 확인 후 부족하면 Exception
            Inventory inventory = inventoryMap.get(requestedDetail.getShoeCode());
            inventory.validateRequestStock(requestedDetail.getCount());

            OrderDetail orderDetail = OrderDetail.create(
                requestedDetail.getShoeCode(),
                shoeMap.get(requestedDetail.getShoeCode()).getPrice(),
                requestedDetail.getCount()
            );
            inventory.deductStock(requestedDetail.getCount());
            updatedInventories.add(inventory);
            details.add(orderDetail);
        }

        // 주문 생성 및 저장
        Order order = Order.create(details);
        Order savedOrder = orderRepository.save(order);
        inventoryRepository.saveAll(updatedInventories);
        return savedOrder;
    }

    @Transactional
    public Order cancelOrder(Long orderNo) {

        Order order = orderRepository.findByOrderNo(orderNo)
            .orElseThrow(OrderService::orderNotFoundException);
        order.totalCancel();

        for (OrderDetail orderDetail : order.getDetails()) {

            Long shoeCode = orderDetail.getShoeCode();
            Inventory inventory = inventoryRepository.findByShoeCode(shoeCode)
                .orElseThrow(Inventory::inventoryNotFoundException);
            inventory.restock(orderDetail.getCount());
            inventoryRepository.save(inventory);
        }

        return orderRepository.save(order);
    }

    @Transactional
    public Order partialCancel(Long orderNo, Long shoeCode, Long removeCount) {

        Order order = orderRepository.findByOrderNo(orderNo)
            .orElseThrow(OrderService::orderNotFoundException);
        order.partialCancel(shoeCode, removeCount);

        Inventory inventory = inventoryRepository.findByShoeCode(shoeCode)
            .orElseThrow(Inventory::inventoryNotFoundException);
        inventory.restock(removeCount);
        inventoryRepository.save(inventory);

        return orderRepository.save(order);
    }

    @Transactional
    public Order simpleUpdateOrder(Order order) {

        return orderRepository.save(order);
    }

    private static IllegalArgumentException orderNotFoundException() {

        return new IllegalArgumentException("Order not found.");
    }
}
