package com.abcmart.shoestore.discount.application;

import com.abcmart.shoestore.discount.domain.DiscountPolicy;
import com.abcmart.shoestore.order.domain.Order;
import com.abcmart.shoestore.order.domain.discount.OrderDetailDiscount;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final ObjectMapper objectMapper;

    private final List<DiscountPolicy> fixedPolicies;

    public List<OrderDetailDiscount> applyDiscounts(Order order, List<DiscountPolicy> dynamicPolicies) {

        try {

            Order copiedOrder = deepCopy(order);

            List<OrderDetailDiscount> orderDetailDiscounts = new ArrayList<>();
            for (DiscountPolicy discountPolicy : fixedPolicies) {

                orderDetailDiscounts.addAll(calculateDiscounts(copiedOrder, discountPolicy));
            }

            for(DiscountPolicy discountPolicy : dynamicPolicies) {

                orderDetailDiscounts.addAll(calculateDiscounts(copiedOrder, discountPolicy));
            }

            return orderDetailDiscounts;

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return List.of();
    }

    private static List<OrderDetailDiscount> calculateDiscounts(Order copiedOrder,
        DiscountPolicy discountPolicy) {

        List<OrderDetailDiscount> orderDetailDiscounts = discountPolicy.apply(copiedOrder);
        orderDetailDiscounts.forEach(discount ->
            copiedOrder.getDetails().stream()
                .filter(
                    detail -> detail.getOrderDetailNo().equals(discount.getOrderDetailNo())
                )
                .findFirst()
                .ifPresent(detail -> detail.applyDiscount(discount))
        );
        return orderDetailDiscounts;
    }

    public Order deepCopy(Order order) throws JsonProcessingException {

        return objectMapper.readValue(objectMapper.writeValueAsString(order), Order.class);
    }
}
