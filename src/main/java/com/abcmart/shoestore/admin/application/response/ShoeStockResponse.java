package com.abcmart.shoestore.admin.application.response;

import com.abcmart.shoestore.inventory.domain.Inventory;
import com.abcmart.shoestore.shoe.domain.Shoe;
import java.math.BigDecimal;

public record ShoeStockResponse(
    Long shoeCode,
    String shoeName,
    String color,
    int size,
    BigDecimal price,
    Long stock
) {

    public static ShoeStockResponse of(Shoe shoe, Inventory inventory) {

        return new ShoeStockResponse(
            shoe.getShoeCode(),
            shoe.getShoeName(),
            shoe.getColor(),
            shoe.getSize(),
            shoe.getPrice(),
            inventory.getStock()
        );
    }
}
