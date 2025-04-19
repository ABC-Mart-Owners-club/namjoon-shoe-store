package com.abcmart.shoestore.testutil;

import com.abcmart.shoestore.entity.ShoeEntity;
import java.math.BigDecimal;

public class ShoeTestEntityFactory extends TestEntityFactory<ShoeEntity> {

    public static ShoeEntity create(Long shoeCode, String shoeName, String color, int size, BigDecimal price) {

        ShoeEntity shoeEntity = ShoeEntity.create(shoeName, color, size, price);
        setId(shoeEntity, "shoeCode", shoeCode);
        return shoeEntity;
    }
}
