package com.abcmart.shoestore.payment.domain;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public abstract class Payment {

    @Id
    private String id;

    @NotNull
    private PaymentType type;

    @NotNull
    private BigDecimal paidAmount;

    //region Constructor
    protected Payment(PaymentType type, BigDecimal paidAmount) {

        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.paidAmount = paidAmount;
    }
    //endregion

    protected BigDecimal updatePaidAmount(BigDecimal paidAmount) {

        this.paidAmount = paidAmount;
        return paidAmount;
    }

    public void updatePaidAmountToZero() {

        this.paidAmount = BigDecimal.ZERO;
    }

    public boolean validateAvailableCancel(BigDecimal cancelledAmount) {

        return getPaidAmount().compareTo(cancelledAmount) >= 0;
    }

    public BigDecimal partialCancel(BigDecimal amountToCancel) {

        BigDecimal cancelAmount = getPaidAmount().min(amountToCancel);
        updatePaidAmount(getPaidAmount().subtract(cancelAmount));
        return cancelAmount;
    }
}
