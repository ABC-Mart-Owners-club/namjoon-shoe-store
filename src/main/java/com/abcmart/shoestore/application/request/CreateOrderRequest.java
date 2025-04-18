package com.abcmart.shoestore.application.request;

import com.abcmart.shoestore.tool.PaymentType;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateOrderRequest {

    private final List<CreateOrderDetailRequest> orderDetails;
    private final PaymentType paymentType;

    private CreateOrderRequest(List<CreateOrderDetailRequest> orderDetails, PaymentType paymentType) {
        this.orderDetails = orderDetails;
        this.paymentType = paymentType;
    }

    public static CreateOrderRequest create(List<CreateOrderDetailRequest> orderDetails, PaymentType paymentType) {

        return new CreateOrderRequest(orderDetails, paymentType);
    }

    @Getter
    public static class CreateOrderDetailRequest {

        private final Long shoeCode;
        private final Long count;

        private CreateOrderDetailRequest(Long shoeCode, Long count) {
            this.shoeCode = shoeCode;
            this.count = count;
        }

        public static CreateOrderDetailRequest create(Long shoeCode, Long count) {

            return new CreateOrderDetailRequest(shoeCode, count);
        }
    }
}
