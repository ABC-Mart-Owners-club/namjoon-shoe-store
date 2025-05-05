package com.abcmart.shoestore.shoe.domain;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Shoe {

    @Id
    private Long shoeCode;

    @NotEmpty
    private String shoeName;

    private String color;

    private int size;

    @NotNull
    private BigDecimal price;

    private Shoe(String shoeName, String color, int size, BigDecimal price) {

        this.shoeName = shoeName;
        this.color = color;
        this.size = size;
        this.price = price;
    }

    public static Shoe create(String shoeName, String color, int size, BigDecimal price) {

        return new Shoe(shoeName, color, size, price);
    }
}
