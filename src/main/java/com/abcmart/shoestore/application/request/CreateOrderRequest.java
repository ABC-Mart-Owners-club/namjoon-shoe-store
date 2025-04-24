package com.abcmart.shoestore.application.request;

import com.abcmart.shoestore.tool.PaymentType;
import lombok.Getter;

import java.util.List;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateOrderRequest {

    private final List<CreateOrderDetailRequest> orderDetails;
    private final PaymentType paymentType;

    @Getter
    @RequiredArgsConstructor
    public static class CreateOrderDetailRequest {

        private final Long shoeCode;
        private final Long count;
    }
}
