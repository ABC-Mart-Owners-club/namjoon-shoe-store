package com.abcmart.shoestore.inventory.domain;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Inventory {

    @Id
    private Long shoeCode;

    @NotNull
    private Long stock;

    //region Constructor
    private Inventory(Long shoeCode, Long stock) {

        this.shoeCode = shoeCode;
        this.stock = stock;
    }
    //endregion

    public static Inventory create(Long shoeCode, Long stock) {

        return new Inventory(shoeCode, stock);
    }

    public Inventory restock(Long stock) {

        this.stock += stock;
        return this;
    }

    public Inventory deductStock(Long stock) {

        this.stock -= stock;
        return this;
    }

    public void validateRequestStock(Long requestStock) {

        if (this.stock < requestStock) {
            throw insufficientStockException();
        }
    }

    public static IllegalArgumentException inventoryNotFoundException() {

        return new IllegalArgumentException("Inventory not found");
    }

    private static IllegalArgumentException insufficientStockException() {

        return new IllegalArgumentException("The stock is insufficient.");
    }
}
