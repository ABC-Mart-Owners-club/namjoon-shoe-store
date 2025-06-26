package com.abcmart.shoestore.discount.domain.policy;

import com.abcmart.shoestore.coupon.domain.Coupon;
import com.abcmart.shoestore.discount.domain.DiscountPolicy;
import com.abcmart.shoestore.discount.domain.DiscountType;
import com.abcmart.shoestore.order.domain.Order;
import com.abcmart.shoestore.order.domain.OrderDetail;
import com.abcmart.shoestore.order.domain.discount.OrderDetailDiscount;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class CouponDiscountPolicy implements DiscountPolicy {

    private final Coupon coupon;

    //region constructor
    private CouponDiscountPolicy(Coupon coupon) {
        this.coupon = coupon;
    }
    //endregion

    public static CouponDiscountPolicy of(Coupon coupon) {

        return new CouponDiscountPolicy(coupon);
    }

    @Override
    public DiscountType getDiscountType() {
        return DiscountType.COUPON;
    }

    @Override
    public List<OrderDetailDiscount> apply(Order order) {

        List<OrderDetail> details = order.getDetails().stream()
            .filter(OrderDetail::isNormal)
            .toList();

        BigDecimal totalAmount = order.getCurrentTotalAmount();
        BigDecimal couponAmount = totalAmount.multiply(coupon.getDiscountRate())
            .setScale(0, RoundingMode.DOWN);

        List<OrderDetailDiscount> result = new ArrayList<>();
        BigDecimal allocated = BigDecimal.ZERO; // 분배된 할인 누적합

        // 상품별로 금액 비율만큼 할인액 분배
        for (int i = 0; i < details.size(); i++) {

            OrderDetail detail = details.get(i);
            BigDecimal itemTotal = detail.getTotalAmount();
            BigDecimal proportion = itemTotal.divide(totalAmount, 10, RoundingMode.HALF_UP);
            BigDecimal discount = couponAmount.multiply(proportion).setScale(0, RoundingMode.FLOOR);

            // 마지막 상품에 나머지 금액 반영
            if (i == details.size() - 1) {
                discount = couponAmount.subtract(allocated);
            } else {
                allocated = allocated.add(discount);
            }

            result.add(OrderDetailDiscount.of(
                detail.getOrderDetailNo(),
                DiscountType.COUPON,
                discount
            ));
        }

        return result;
    }
}
