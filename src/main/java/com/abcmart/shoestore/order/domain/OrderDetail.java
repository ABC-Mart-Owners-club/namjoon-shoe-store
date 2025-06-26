package com.abcmart.shoestore.order.domain;

import com.abcmart.shoestore.order.domain.discount.OrderDetailDiscount;
import com.abcmart.shoestore.utils.ShoeProductCodeUtils;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderDetail {

    @Id
    private Long orderDetailNo;

    @NotNull
    private OrderStatus orderDetailStatus;

    @NotNull
    private String shoeProductCode;

    @NotNull
    private BigDecimal unitPrice;

    @NotNull
    private Long count;

    private List<OrderDetailDiscount> discounts = new ArrayList<>();

    //region Constructor
    private OrderDetail(Long shoeCode, LocalDate stockedDate, BigDecimal unitPrice, Long count) {

        this.orderDetailStatus = OrderStatus.NORMAL;
        this.shoeProductCode = ShoeProductCodeUtils.generate(shoeCode, stockedDate);
        this.unitPrice = unitPrice;
        this.count = count;
    }
    //endregion

    public Long getShoeCode() {

        return ShoeProductCodeUtils.parse(shoeProductCode).shoeCode();
    }

    public LocalDate getStockedDate() {

        return ShoeProductCodeUtils.parse(shoeProductCode).stockedDate();
    }

    public BigDecimal getTotalOriginAmount() {

        return unitPrice.multiply(BigDecimal.valueOf(count));
    }

    public BigDecimal getTotalAmount() {

        BigDecimal originAmount = unitPrice.multiply(BigDecimal.valueOf(count));
        return originAmount.subtract(getTotalDiscountAmount());
    }

    public BigDecimal getTotalDiscountAmount() {

        return discounts.stream()
            .map(OrderDetailDiscount::getDiscountedAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static OrderDetail create(Long shoeCode, LocalDate stockedDate, BigDecimal unitPrice, Long count) {

        return new OrderDetail(shoeCode, stockedDate, unitPrice, count);
    }

    protected OrderDetail totalCancel() {

        this.orderDetailStatus = OrderStatus.CANCEL;
        this.count = 0L;
        return this;
    }

    protected BigDecimal partialCancel(Long removeCount) {

        if (removeCount > this.count) {
            throw new IllegalArgumentException("Remove count exceeds order count");
        }

        this.count -= removeCount;
        if (this.count == 0) {
            this.orderDetailStatus = OrderStatus.CANCEL;
        }
        return this.unitPrice.multiply(BigDecimal.valueOf(removeCount));
    }

    public void applyDiscount(OrderDetailDiscount discount) {

        this.discounts.add(discount);
    }

    public boolean isNormal() {

        return this.orderDetailStatus == OrderStatus.NORMAL;
    }

    public boolean isCancel() {

        return this.orderDetailStatus == OrderStatus.CANCEL;
    }
}
