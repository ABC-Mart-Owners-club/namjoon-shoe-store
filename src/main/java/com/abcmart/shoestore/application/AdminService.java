package com.abcmart.shoestore.application;

import com.abcmart.shoestore.application.response.ShoeSaleAmountResponse;
import com.abcmart.shoestore.application.response.ShoeSaleAmountResponse.CreditCardSaleAmountResponse;
import com.abcmart.shoestore.application.response.ShoeSaleCountResponse;
import com.abcmart.shoestore.application.response.ShoeSaleCountResponse.SoldShoe;
import com.abcmart.shoestore.domain.CardPayment;
import com.abcmart.shoestore.domain.OrderDetail;
import com.abcmart.shoestore.domain.Shoe;
import com.abcmart.shoestore.dto.ShoeDto;
import com.abcmart.shoestore.repository.OrderRepository;
import com.abcmart.shoestore.repository.PaymentRepository;
import com.abcmart.shoestore.repository.ShoeRepository;
import com.abcmart.shoestore.tool.CreditCardType;
import java.math.BigDecimal;
import java.util.ArrayList;
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
public class AdminService {

    private final OrderRepository orderRepository;
    private final ShoeRepository shoeRepository;
    private final PaymentRepository paymentRepository;

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

    @Transactional(readOnly = true)
    public ShoeSaleAmountResponse getShoeSaleAmountByCreditCardType() {

        List<CardPayment> creditCardCardPayments = paymentRepository.findAllCreditCardPayments();

        Map<CreditCardType, List<CardPayment>> creditCardGrouping = creditCardCardPayments.stream()
            .filter(payment -> Objects.nonNull(payment.getCreditCardType()))
            .collect(Collectors.groupingBy(CardPayment::getCreditCardType));

        List<CreditCardSaleAmountResponse> creditCardSaleAmounts = new ArrayList<>();
        for (CreditCardType creditCardType : creditCardGrouping.keySet()) {

            List<CardPayment> cardPayments = creditCardGrouping.get(creditCardType);
            BigDecimal addedAmount = cardPayments.stream()
                .map(CardPayment::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            creditCardSaleAmounts.add(CreditCardSaleAmountResponse.of(creditCardType, addedAmount));
        }

        return ShoeSaleAmountResponse.from(creditCardSaleAmounts);
    }
}
