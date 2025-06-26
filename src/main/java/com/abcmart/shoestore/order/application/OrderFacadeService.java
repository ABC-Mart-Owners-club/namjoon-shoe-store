package com.abcmart.shoestore.order.application;

import com.abcmart.shoestore.coupon.application.CouponService;
import com.abcmart.shoestore.coupon.domain.Coupon;
import com.abcmart.shoestore.discount.application.DiscountService;
import com.abcmart.shoestore.discount.domain.DiscountPolicy;
import com.abcmart.shoestore.discount.domain.policy.CouponDiscountPolicy;
import com.abcmart.shoestore.inventory.application.InventoryService;
import com.abcmart.shoestore.inventory.dto.AvailableDeductionStock;
import com.abcmart.shoestore.order.application.request.CreateOrderRequest;
import com.abcmart.shoestore.order.domain.Order;
import com.abcmart.shoestore.order.domain.discount.OrderDetailDiscount;
import com.abcmart.shoestore.order.dto.OrderDto;
import com.abcmart.shoestore.payment.application.PaymentService;
import com.abcmart.shoestore.payment.domain.Payment;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderFacadeService {

    private final InventoryService inventoryService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final DiscountService discountService;
    private final PaymentService paymentService;

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request) {

        // 재고 준비 및 재고 수량 감소
        Map<Long, List<AvailableDeductionStock>> availableStockMap =
            inventoryService.deductStockAndFindAvailableOrElseThrow(
                request.orderDetails()
            );

        // 주문 생성
        Order order = orderService.createOrder(availableStockMap);

        List<DiscountPolicy> dynamicDiscountPolicies = new ArrayList<>();
        if (request.existCoupon()) {

            Coupon coupon = couponService.findByCode(request.couponCode());
            dynamicDiscountPolicies.add(CouponDiscountPolicy.of(coupon));
        }
        List<OrderDetailDiscount> orderDetailDiscounts = discountService.applyDiscounts(
            order, dynamicDiscountPolicies
        );
        order.updateDetailDiscounts(orderDetailDiscounts);

        // 결제 데이터 생성
        List<Payment> payments = paymentService.createPayment(request.payments());

        // 주문에 결제 데이터 첨부
        order.updatePaymentIds(payments.stream().map(Payment::getId).toList());
        Order updatedOrder = orderService.simpleUpdateOrder(order);

        return OrderDto.from(updatedOrder, payments);
    }

    @Transactional
    public OrderDto cancelOrder(Long orderNo) {

        Order order = orderService.cancelOrder(orderNo);
        List<Payment> payments = paymentService.cancelAllByPaymentIds(order.getPaymentIds());
        return OrderDto.from(order, payments);
    }

    @Transactional
    public OrderDto partialCancel(Long orderNo, String shoeProductCode, Long removeCount) {

        Order storedOrder = orderService.findOrderByOrderNo(orderNo);
        Order partialCancelledOrder = orderService.partialCancel(orderNo, shoeProductCode, removeCount);

        // 결제 취소할 금액 계산
        BigDecimal cancelTargetAmount = storedOrder.getAmountToCancel(partialCancelledOrder.getCurrentTotalAmount());
        List<Payment> cancelledPayments = paymentService.partialCancel(
            partialCancelledOrder.getPaymentIds(), cancelTargetAmount
        );
        return OrderDto.from(partialCancelledOrder, cancelledPayments);
    }
}
