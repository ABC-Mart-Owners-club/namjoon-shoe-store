package com.abcmart.shoestore.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@NoArgsConstructor
public class ShoeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long shoeCode;

    private String shoeName;

    private String color;

    private int size;

    private BigDecimal price;

    private ShoeEntity(String shoeName, String color, int size, BigDecimal price) {

        this.shoeName = shoeName;
        this.color = color;
        this.size = size;
        this.price = price;
    }

    public ShoeEntity create(String shoeName, String color, int size, BigDecimal price) {

        return new ShoeEntity(shoeName, color, size, price);
    }
}
