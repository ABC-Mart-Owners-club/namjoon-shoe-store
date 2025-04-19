package com.abcmart.shoestore.tool;

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
