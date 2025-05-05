package com.abcmart.shoestore.application.response;

import com.abcmart.shoestore.tool.CreditCardType;
import java.math.BigDecimal;
import java.util.List;

public record ShoeSaleAmountResponse(
    List<CreditCardSaleAmountResponse> creditCardSaleAmounts, int totalElements
) {

    public static ShoeSaleAmountResponse from(List<CreditCardSaleAmountResponse> creditCardSaleAmounts) {

        return new ShoeSaleAmountResponse(creditCardSaleAmounts, creditCardSaleAmounts.size());
    }

    public record CreditCardSaleAmountResponse(
        CreditCardType creditCardType, BigDecimal totalAmount
    ) {

        public static CreditCardSaleAmountResponse of(CreditCardType creditCardType, BigDecimal totalAmount) {

            return new CreditCardSaleAmountResponse(creditCardType, totalAmount);
        }
    }
}
