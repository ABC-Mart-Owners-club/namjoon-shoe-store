package com.abcmart.shoestore.application;

import com.abcmart.shoestore.application.response.ShoeSaleCountResponse;
import com.abcmart.shoestore.application.response.ShoeSaleCountResponse.SoldShoe;
import com.abcmart.shoestore.domain.OrderDetail;
import com.abcmart.shoestore.domain.Shoe;
import com.abcmart.shoestore.dto.ShoeDto;
import com.abcmart.shoestore.repository.OrderRepository;
import com.abcmart.shoestore.repository.ShoeRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final OrderRepository orderRepository;
    private final ShoeRepository shoeRepository;

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
