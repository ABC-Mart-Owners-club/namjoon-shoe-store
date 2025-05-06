package com.abcmart.shoestore.order.domain;

import com.abcmart.shoestore.payment.domain.Payment;
import com.abcmart.shoestore.shoe.domain.Shoe;
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
    private Map<String, Payment> payments;

    private Order(List<OrderDetail> details, List<Payment> payments) {

        this.status = OrderStatus.NORMAL;
        this.details = details;
        this.totalAmount = details.stream()
            .map(detail ->
                detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getCount()))
            )
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.payments = payments.stream()
            .collect(Collectors.toMap(Payment::getId, Function.identity()));
    }

    public static Order create(List<OrderDetail> detailEntities, List<Payment> payments) {

        return new Order(detailEntities, payments);
    }

    public void totalCancel() {

        validateAvailableCancel();

        this.payments.values().forEach(Payment::updatePaidAmountToZero);
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
        Optional<Payment> availablePayment = this.payments.values().stream()
            .filter(payment -> payment.validateAvailableCancel(totalCancelAmount))
            .findFirst();
        if (availablePayment.isPresent()) {

            String paymentId = availablePayment.get().getId();
            Optional<Payment> targetPayment = Optional.ofNullable(this.payments.get(paymentId));
            targetPayment.orElseThrow(
                () -> new IllegalArgumentException("Payment not found")
            ).partialCancel(totalCancelAmount);

        } else {

            BigDecimal remainCancelAmount = totalCancelAmount;
            List<Payment> targetPayments = this.payments.values().stream().toList();
            for (Payment payment : targetPayments) {
                BigDecimal canceledAmount = payment.partialCancel(remainCancelAmount);
                remainCancelAmount = remainCancelAmount.subtract(canceledAmount);
            }
            this.payments = targetPayments.stream()
                .collect(Collectors.toMap(Payment::getId, Function.identity()));
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
