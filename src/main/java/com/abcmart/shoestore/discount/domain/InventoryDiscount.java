package com.abcmart.shoestore.discount.domain;

import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class InventoryDiscount extends Discount {

    //region constructor
    public InventoryDiscount(int discountRate) {
        super(discountRate);
    }
    //endregion

    @Override
    public DiscountType getDiscountType() {
        return DiscountType.INVENTORY;
    }

    @Override
    public BigDecimal calculateDiscount() {
        return null;
    }
}
