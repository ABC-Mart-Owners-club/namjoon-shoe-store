package com.abcmart.shoestore.inventory.domain;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Inventory {

    @Id
    private Long shoeCode;

    @NotNull
    private LocalDate stockedDate;

    @NotNull
    private Long stock;

    //region Constructor
    private Inventory(Long shoeCode, LocalDate stockedDate, Long stock) {

        this.shoeCode = shoeCode;
        this.stockedDate = stockedDate;
        this.stock = stock;
    }
    //endregion

    public static Inventory create(Long shoeCode, Long stock) {

        return new Inventory(shoeCode, LocalDate.now(), stock);
    }

    public Inventory restock(Long stock) {

        this.stock += stock;
        return this;
    }

    public Long deductStock(Long requestedQuantity) {

        validateRequestStock(requestedQuantity);
        long deducted = Math.min(this.stock, requestedQuantity);
        this.stock -= deducted;
        return deducted;
    }

    private void validateRequestStock(Long requestedQuantity) {

        if (this.stock < requestedQuantity) {
            throw insufficientStockException();
        }
    }

    public static IllegalArgumentException inventoryNotFoundException() {

        return new IllegalArgumentException("Inventory not found");
    }

    public static IllegalArgumentException insufficientStockException() {

        return new IllegalArgumentException("The stock is insufficient.");
    }
}
