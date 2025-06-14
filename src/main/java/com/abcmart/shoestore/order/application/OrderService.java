package com.abcmart.shoestore.order.application;

import com.abcmart.shoestore.inventory.domain.Inventory;
import com.abcmart.shoestore.inventory.dto.AvailableDeductionStock;
import com.abcmart.shoestore.inventory.repository.InventoryRepository;
import com.abcmart.shoestore.order.domain.Order;
import com.abcmart.shoestore.order.domain.OrderDetail;
import com.abcmart.shoestore.order.repository.OrderRepository;
import com.abcmart.shoestore.shoe.domain.Shoe;
import com.abcmart.shoestore.shoe.repository.ShoeRepository;
import com.abcmart.shoestore.utils.ShoeProductCodeUtils;
import com.abcmart.shoestore.utils.ShoeProductCodeUtils.ParsedResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    public Order createOrder(Map<Long, List<AvailableDeductionStock>> availableStockMap) {

        // 주문을 원하는 신발의 가격 조회
        List<Long> shoeCodes = availableStockMap.keySet().stream().toList();
        Map<Long, Shoe> shoeMap = shoeRepository.findAllByShoeCodes(shoeCodes).stream()
            .collect(Collectors.toMap(Shoe::getShoeCode, Function.identity()));

        ArrayList<OrderDetail> details = new ArrayList<>();
        for (Long shoeCode : shoeCodes) {

            List<AvailableDeductionStock> availableShoeStockList = availableStockMap.get(shoeCode);
            BigDecimal price = shoeMap.get(shoeCode).getPrice();

            List<OrderDetail> orderDetails = availableShoeStockList.stream()
                .map(stock -> OrderDetail.create(
                    shoeCode, stock.getStockedDate(), price, stock.getDeductedQuantity()
                ))
                .toList();
            details.addAll(orderDetails);
        }

        // 주문 생성 및 저장
        Order order = Order.create(details);
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderNo) {

        Order order = orderRepository.findByOrderNo(orderNo)
            .orElseThrow(OrderService::orderNotFoundException);
        order.totalCancel();

        for (OrderDetail orderDetail : order.getDetails()) {

            ParsedResult parsedResult = ShoeProductCodeUtils.parse(orderDetail.getShoeProductCode());
            Long shoeCode = parsedResult.shoeCode();
            LocalDate stockedDate = parsedResult.stockedDate();

            Inventory inventory = inventoryRepository.findByShoeCodeAndStockedDate(shoeCode, stockedDate)
                .orElseThrow(Inventory::inventoryNotFoundException);
            inventory.restock(orderDetail.getCount());
            inventoryRepository.save(inventory);
        }

        return orderRepository.save(order);
    }

    @Transactional
    public Order partialCancel(Long orderNo, String shoeProductCode, Long removeCount) {

        Order order = orderRepository.findByOrderNo(orderNo)
            .orElseThrow(OrderService::orderNotFoundException);
        order.partialCancel(shoeProductCode, removeCount);

        ParsedResult parsedResult = ShoeProductCodeUtils.parse(shoeProductCode);
        Long shoeCode = parsedResult.shoeCode();
        LocalDate stockedDate = parsedResult.stockedDate();

        Inventory inventory = inventoryRepository.findByShoeCodeAndStockedDate(shoeCode, stockedDate)
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
