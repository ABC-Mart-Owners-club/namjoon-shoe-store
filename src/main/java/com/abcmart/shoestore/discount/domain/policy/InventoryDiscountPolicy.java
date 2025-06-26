package com.abcmart.shoestore.discount.domain.policy;

import com.abcmart.shoestore.discount.domain.DiscountPolicy;
import com.abcmart.shoestore.discount.domain.DiscountType;
import com.abcmart.shoestore.order.domain.Order;
import com.abcmart.shoestore.order.domain.OrderDetail;
import com.abcmart.shoestore.order.domain.discount.OrderDetailDiscount;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class InventoryDiscountPolicy implements DiscountPolicy {

    @Override
    public DiscountType getDiscountType() {
        return DiscountType.INVENTORY;
    }

    @Override
    public List<OrderDetailDiscount> apply(Order order) {

        return order.getDetails().stream()
            .filter(OrderDetail::isNormal)
            .map(orderDetail -> {

                BigDecimal discountRate = getDiscountRateByStockedDate(orderDetail.getStockedDate());
                BigDecimal discountedAmount = orderDetail.getTotalAmount()
                    .multiply(BigDecimal.ONE.subtract(discountRate))
                    .setScale(0, RoundingMode.DOWN);

                return OrderDetailDiscount.of(
                    orderDetail.getOrderDetailNo(), getDiscountType(), discountedAmount
                );
            })
            .toList();
    }

    private static BigDecimal getDiscountRateByStockedDate(LocalDate stockedDate) {

        final int PERCENT_50 = 50;
        final int PERCENT_30 = 30;

        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        LocalDate oneMonthAgo = today.minusMonths(1);
        if (stockedDate.isBefore(oneMonthAgo)) {
            return BigDecimal.valueOf(PERCENT_50)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        }

        LocalDate oneWeekAgo = today.minusWeeks(1);
        if (stockedDate.isAfter(oneWeekAgo)) {
            return BigDecimal.valueOf(PERCENT_30)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }
}
