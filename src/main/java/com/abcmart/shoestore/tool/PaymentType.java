package com.abcmart.shoestore.tool;

public enum PaymentType {

    CASH
    ;

    public boolean isCash() {

        return this == CASH;
    }
}
