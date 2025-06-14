package com.abcmart.shoestore.discount.domain;

public enum DiscountType {

    COUPON,
    INVENTORY
    ;

    public boolean isCoupon() {

        return this == COUPON;
    }

    public boolean isInventory() {

        return this == INVENTORY;
    }
}
