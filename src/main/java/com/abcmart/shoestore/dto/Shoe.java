package com.abcmart.shoestore.dto;

import com.abcmart.shoestore.entity.ShoeEntity;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class Shoe {

    private Long shoeCode;
    private String shoeName;
    private String color;
    private int size;
    private BigDecimal price;

    private Shoe(Long shoeCode, String shoeName, String color, int size, BigDecimal price) {

        this.shoeCode = shoeCode;
        this.shoeName = shoeName;
        this.color = color;
        this.size = size;
        this.price = price;
    }

    public static Shoe from(ShoeEntity shoeEntity) {

        return new Shoe(
            shoeEntity.getShoeCode(),
            shoeEntity.getShoeName(),
            shoeEntity.getColor(),
            shoeEntity.getSize(),
            shoeEntity.getPrice()
        );
    }
}
