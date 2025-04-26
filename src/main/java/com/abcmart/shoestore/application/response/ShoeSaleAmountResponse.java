package com.abcmart.shoestore.application.response;

import com.abcmart.shoestore.tool.CreditCardType;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;

@Getter
public class ShoeSaleAmountResponse {

    private final List<CreditCardSaleAmountResponse> creditCardSaleAmounts;
    private final int totalElements;

    private ShoeSaleAmountResponse(List<CreditCardSaleAmountResponse> creditCardSaleAmounts) {

        this.creditCardSaleAmounts = creditCardSaleAmounts;
        this.totalElements = creditCardSaleAmounts.size();
    }

    public static ShoeSaleAmountResponse from(List<CreditCardSaleAmountResponse> creditCardSaleAmounts) {

        return new ShoeSaleAmountResponse(creditCardSaleAmounts);
    }

    @Getter
    public static class CreditCardSaleAmountResponse {

        private CreditCardType creditCardType;
        private BigDecimal totalAmount;

        private CreditCardSaleAmountResponse(CreditCardType creditCardType, BigDecimal totalAmount) {

            this.creditCardType = creditCardType;
            this.totalAmount = totalAmount;
        }

        public static CreditCardSaleAmountResponse of(CreditCardType creditCardType, BigDecimal totalAmount) {

            return new CreditCardSaleAmountResponse(creditCardType, totalAmount);
        }
    }
}
