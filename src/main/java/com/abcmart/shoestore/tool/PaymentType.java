package com.abcmart.shoestore.tool;

public enum PaymentType {

    CASH,
    CREDIT_CARD
    ;

    public boolean isCash() {

        return this == CASH;
    }

    public boolean isCreditCard() {

        return this == CREDIT_CARD;
    }
}
