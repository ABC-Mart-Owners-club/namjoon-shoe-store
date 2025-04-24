package com.abcmart.shoestore.domain;

import com.abcmart.shoestore.tool.OrderStatus;
import jakarta.persistence.Embedded;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Order {

    @Id
    private Long orderNo;

    @NotNull
    private OrderStatus status;

    @NotEmpty
    private List<OrderDetail> details;

    @Lob
    @Embedded
    @NotNull
    private OrderPayment orderPayment;

    private Order(List<OrderDetail> details, OrderPayment orderPayment) {

        this.status = OrderStatus.NORMAL;
        this.details = details;
        this.orderPayment = orderPayment;
    }

    public static Order create(List<OrderDetail> detailEntities, OrderPayment orderPayment) {

        return new Order(detailEntities, orderPayment);
    }

    public void totalCancel() {

        validateAvailableCancel();

        this.status = OrderStatus.CANCEL;
    }

    public Order partialCancel(Shoe shoe, Long removeCount) {

        validateAvailableCancel();

        Map<Long, OrderDetail> detailEntityMap = this.details.stream()
                .collect(Collectors.toMap(OrderDetail::getShoeCode, Function.identity()));

        OrderDetail orderDetail = detailEntityMap.get(shoe.getShoeCode());
        if (Objects.isNull(orderDetail)) {
            throw new IllegalArgumentException("ShoeCode not found in order details");
        }

        orderDetail.partialCancel(removeCount);
        if (validateAllCancelled()) {
            this.status = OrderStatus.CANCEL;
        }

        BigDecimal cancelledAmount = shoe.getPrice().multiply(BigDecimal.valueOf(removeCount));
        orderPayment.partialCancel(cancelledAmount);

        return this;
    }

    private void validateAvailableCancel() {

        if (this.status.isCancel()) {
            throw new IllegalStateException("Order status already cancelled");
        }
    }

    private boolean validateAllCancelled() {

        return this.status.isCancel() || this.details.stream().noneMatch(OrderDetail::isNormal);
    }
}
