package com.abcmart.shoestore.testutil;

import com.abcmart.shoestore.domain.Shoe;
import java.math.BigDecimal;

public class ShoeTestEntityFactory extends TestEntityFactory<Shoe> {

    public static Shoe create(Long shoeCode, String shoeName, String color, int size, BigDecimal price) {

        Shoe shoe = Shoe.create(shoeName, color, size, price);
        setId(shoe, "shoeCode", shoeCode);
        return shoe;
    }
}
