package com.abcmart.shoestore.order.domain;

public enum OrderStatus {

    NORMAL,
    CANCEL
    ;

    public boolean isNormal() {

        return this == NORMAL;
    }

    public boolean isCancel() {

        return this == CANCEL;
    }
}
