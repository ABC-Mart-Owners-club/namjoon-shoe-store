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
    private OrderStatus orderStatus;

    @NotNull
    private Long shoeCode;

    @NotNull
    private BigDecimal unitPrice;

    @NotNull
    private Long count;

    private OrderDetail(Long shoeCode, BigDecimal unitPrice, Long count) {

        this.orderStatus = OrderStatus.NORMAL;
        this.shoeCode = shoeCode;
        this.unitPrice = unitPrice;
        this.count = count;
    }

    public static OrderDetail create(Long shoeCode, BigDecimal unitPrice, Long count) {

        return new OrderDetail(shoeCode, unitPrice, count);
    }

    protected OrderDetail partialCancel(Long removeCount) {

        if (removeCount > this.count) {
            throw new IllegalArgumentException("Remove count exceeds order count");
        }

        this.count -= removeCount;
        if (this.count == 0) {
            this.orderStatus = OrderStatus.CANCEL;
        }
        return this;
    }

    public boolean isNormal() {

        return this.orderStatus == OrderStatus.NORMAL;
    }

    public boolean isCancel() {

        return this.orderStatus == OrderStatus.CANCEL;
    }
}
