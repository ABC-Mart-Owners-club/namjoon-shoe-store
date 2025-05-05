package com.abcmart.shoestore.order.domain;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
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
    private Long count;

    private OrderDetail(Long shoeCode, Long count) {

        this.orderStatus = OrderStatus.NORMAL;
        this.shoeCode = shoeCode;
        this.count = count;
    }

    public static OrderDetail create(Long shoeCode, Long count) {

        return new OrderDetail(shoeCode, count);
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
