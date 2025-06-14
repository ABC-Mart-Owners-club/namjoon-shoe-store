package com.abcmart.shoestore.discount.domain;

import java.math.BigDecimal;
import lombok.Getter;

@Getter
public abstract class Discount {

    private final int discountRate;

    //region constructor
    protected Discount(int discountRate) {
        this.discountRate = discountRate;
    }
    //endregion

    public abstract DiscountType getDiscountType();

    public abstract BigDecimal calculateDiscount();
}
