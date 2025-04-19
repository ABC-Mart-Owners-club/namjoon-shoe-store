package com.abcmart.shoestore.entity;

import com.abcmart.shoestore.tool.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class OrderDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderDetailNo;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    private OrderEntity order;

    private Long shoeCode;

    private Long count;

    private OrderDetailEntity(Long shoeCode, Long count) {

        this.orderStatus = OrderStatus.NORMAL;
        this.shoeCode = shoeCode;
        this.count = count;
    }

    public static OrderDetailEntity create(Long shoeCode, Long count) {

        return new OrderDetailEntity(shoeCode, count);
    }

    protected OrderDetailEntity partialCancel(Long removeCount) {

        if (removeCount > this.count) {
            throw new IllegalArgumentException("Remove count exceeds order count");
        }

        this.count -= removeCount;
        if (this.count == 0) {
            this.orderStatus = OrderStatus.CANCEL;
        }
        return this;
    }

    public void link(OrderEntity orderEntity) {

        this.order = orderEntity;
    }

    public boolean isNormal() {

        return this.orderStatus == OrderStatus.NORMAL;
    }

    public boolean isCancel() {

        return this.orderStatus == OrderStatus.CANCEL;
    }
}
