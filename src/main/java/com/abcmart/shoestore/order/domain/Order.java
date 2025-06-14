package com.abcmart.shoestore.order.domain;

import com.abcmart.shoestore.order.domain.discount.OrderDetailDiscount;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
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

    private List<String> paymentIds;

    //region Constructor
    private Order(List<OrderDetail> details) {

        this.status = OrderStatus.NORMAL;
        this.details = details;
    }
    //endregion

    public static Order create(List<OrderDetail> detailEntities) {

        return new Order(detailEntities);
    }

    public void totalCancel() {

        validateAvailableCancel();

        this.status = OrderStatus.CANCEL;
        this.details.forEach(OrderDetail::totalCancel);
    }

    public BigDecimal partialCancel(String shoeProductCode, Long removeCount) {

        validateAvailableCancel();

        OrderDetail orderDetail = this.details.stream()
            .filter(detail -> detail.getShoeProductCode().equals(shoeProductCode))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("ShoeCode not found in order details"));

        BigDecimal cancelledAmount = orderDetail.partialCancel(removeCount);
        if (validateAllCancelled()) {
            this.status = OrderStatus.CANCEL;
        }

        return cancelledAmount;
    }

    public void updateDetailDiscounts(List<OrderDetailDiscount> detailDiscounts) {

        detailDiscounts.forEach(detailDiscount ->
            details.stream()
                .filter(detail -> detail.getOrderDetailNo().equals(detailDiscount.getOrderDetailNo()))
                .findFirst()
                .ifPresent(detail -> detail.applyDiscount(detailDiscount))
        );
    }

    public void updatePaymentIds(List<String> paymentIds) {

        this.paymentIds = paymentIds;
    }

    public BigDecimal getCurrentTotalAmount() {

        return this.details.stream()
            .filter(OrderDetail::isNormal)
            .map(OrderDetail::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getAmountToCancel(BigDecimal cancelledAmount) {

        return getCurrentTotalAmount().subtract(cancelledAmount);
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
