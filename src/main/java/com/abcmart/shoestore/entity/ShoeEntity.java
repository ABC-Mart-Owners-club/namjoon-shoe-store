package com.abcmart.shoestore.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private Long price;

    private ShoeEntity(String shoeName, String color, int size, Long price) {

        this.shoeName = shoeName;
        this.color = color;
        this.size = size;
        this.price = price;
    }

    public ShoeEntity create(String shoeName, String color, int size, Long price) {

        return new ShoeEntity(shoeName, color, size, price);
    }
}
