package com.abcmart.shoestore.order.domain;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
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
    private Long shoeCode;

    @NotNull
    private BigDecimal unitPrice;

    @NotNull
    private Long count;

    private OrderDetail(Long shoeCode, BigDecimal unitPrice, Long count) {

        this.orderDetailStatus = OrderStatus.NORMAL;
        this.shoeCode = shoeCode;
        this.unitPrice = unitPrice;
        this.count = count;
    }

    public static OrderDetail create(Long shoeCode, BigDecimal unitPrice, Long count) {

        return new OrderDetail(shoeCode, unitPrice, count);
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

    public boolean isNormal() {

        return this.orderDetailStatus == OrderStatus.NORMAL;
    }

    public boolean isCancel() {

        return this.orderDetailStatus == OrderStatus.CANCEL;
    }
}
