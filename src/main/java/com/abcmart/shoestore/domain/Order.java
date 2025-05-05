package com.abcmart.shoestore.domain;

import com.abcmart.shoestore.tool.OrderStatus;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

    @NotNull
    private BigDecimal totalAmount;

    @NotEmpty
    private Map<String, OrderPayment> orderPayments;

    private Order(List<OrderDetail> details, BigDecimal totalAmount, List<OrderPayment> orderPayments) {

        this.status = OrderStatus.NORMAL;
        this.details = details;
        this.totalAmount = totalAmount;
        this.orderPayments = orderPayments.stream()
            .collect(Collectors.toMap(OrderPayment::getId, Function.identity()));
    }

    public static Order create(List<OrderDetail> detailEntities, BigDecimal totalAmount,
        List<OrderPayment> orderPayments) {

        return new Order(detailEntities, totalAmount, orderPayments);
    }

    public void totalCancel() {

        validateAvailableCancel();

        this.orderPayments.values().forEach(OrderPayment::updatePaidAmountToZero);
        this.status = OrderStatus.CANCEL;
    }

    public Order partialCancel(Shoe shoe, Long removeCount) {

        validateAvailableCancel();

        Map<Long, OrderDetail> detailMap = this.details.stream()
                .collect(Collectors.toMap(OrderDetail::getShoeCode, Function.identity()));

        OrderDetail orderDetail = detailMap.get(shoe.getShoeCode());
        if (Objects.isNull(orderDetail)) {
            throw new IllegalArgumentException("ShoeCode not found in order details");
        }

        orderDetail.partialCancel(removeCount);
        if (validateAllCancelled()) {
            this.status = OrderStatus.CANCEL;
        }

        BigDecimal totalCancelAmount = shoe.getPrice().multiply(BigDecimal.valueOf(removeCount));
        Optional<OrderPayment> availablePayment = this.orderPayments.values().stream()
            .filter(payment -> payment.validateAvailableCancel(totalCancelAmount))
            .findFirst();
        if (availablePayment.isPresent()) {

            String paymentId = availablePayment.get().getId();
            Optional<OrderPayment> targetPayment = Optional.ofNullable(this.orderPayments.get(paymentId));
            targetPayment.orElseThrow(
                () -> new IllegalArgumentException("OrderPayment not found")
            ).partialCancel(totalCancelAmount);

        } else {

            BigDecimal remainCancelAmount = totalCancelAmount;
            List<OrderPayment> targetPayments = this.orderPayments.values().stream().toList();
            for (OrderPayment orderPayment : targetPayments) {
                BigDecimal canceledAmount = orderPayment.partialCancel(remainCancelAmount);
                remainCancelAmount = remainCancelAmount.subtract(canceledAmount);
            }
            this.orderPayments = targetPayments.stream()
                .collect(Collectors.toMap(OrderPayment::getId, Function.identity()));
        }

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
