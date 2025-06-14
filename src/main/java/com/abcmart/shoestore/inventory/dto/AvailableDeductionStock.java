package com.abcmart.shoestore.inventory.dto;

import com.abcmart.shoestore.inventory.domain.Inventory;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public class AvailableDeductionStock {

    private Long shoeCode;
    private LocalDate stockedDate;
    private Long deductedQuantity;

    private AvailableDeductionStock(Long shoeCode, LocalDate stockedDate, Long deductedQuantity) {
        this.shoeCode = shoeCode;
        this.stockedDate = stockedDate;
        this.deductedQuantity = deductedQuantity;
    }

    public static AvailableDeductionStock of(Inventory inventory, Long deductedQuantity) {

        return new AvailableDeductionStock(
            inventory.getShoeCode(), inventory.getStockedDate(), deductedQuantity
        );
    }
}
