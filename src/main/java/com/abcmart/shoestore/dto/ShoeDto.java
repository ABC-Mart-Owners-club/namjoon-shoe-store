package com.abcmart.shoestore.dto;

import com.abcmart.shoestore.domain.Shoe;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ShoeDto {

    private Long shoeCode;
    private String shoeName;
    private String color;
    private int size;
    private BigDecimal price;

    private ShoeDto(Long shoeCode, String shoeName, String color, int size, BigDecimal price) {

        this.shoeCode = shoeCode;
        this.shoeName = shoeName;
        this.color = color;
        this.size = size;
        this.price = price;
    }

    public static ShoeDto from(Shoe shoe) {

        return new ShoeDto(
            shoe.getShoeCode(),
            shoe.getShoeName(),
            shoe.getColor(),
            shoe.getSize(),
            shoe.getPrice()
        );
    }
}
