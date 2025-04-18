package com.abcmart.shoestore.entity;

import com.abcmart.shoestore.domain.OrderPayment;
import com.abcmart.shoestore.tool.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Entity
@NoArgsConstructor
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderNo;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(cascade = CascadeType.ALL)
    private List<OrderDetailEntity> details;

    @Lob
    @Embedded
    private OrderPayment orderPayment;

    private OrderEntity(List<OrderDetailEntity> details, OrderPayment orderPayment) {

        this.status = OrderStatus.NORMAL;
        this.details = details;
        this.orderPayment = orderPayment;
    }

    public static OrderEntity create(List<OrderDetailEntity> detailEntities, OrderPayment orderPayment) {

        OrderEntity orderEntity = new OrderEntity(detailEntities, orderPayment);
        orderEntity.getDetails().forEach(orderDetailEntity -> orderDetailEntity.link(orderEntity));
        return orderEntity;
    }

    public void totalCancel() {

        validateAvailableCancel();

        this.status = OrderStatus.CANCEL;
    }

    public OrderEntity partialCancel(Long shoeCode, Long removeCount) {

        validateAvailableCancel();

        Map<Long, OrderDetailEntity> detailEntityMap = this.details.stream()
                .collect(Collectors.toMap(OrderDetailEntity::getShoeCode, Function.identity()));

        OrderDetailEntity orderDetailEntity = detailEntityMap.get(shoeCode);
        if (Objects.isNull(orderDetailEntity)) {
            throw new IllegalArgumentException("ShoeCode not found in order details");
        }

        orderDetailEntity.partialCancel(removeCount);
        if (validateAllCancelled()) {
            this.status = OrderStatus.CANCEL;
        }

        return this;
    }

    private void validateAvailableCancel() {

        if (this.status.isCancel()) {
            throw new IllegalStateException("Order status already cancelled");
        }
    }

    private boolean validateAllCancelled() {

        return this.status.isCancel() || this.details.stream().noneMatch(OrderDetailEntity::isNormal);
    }
}
