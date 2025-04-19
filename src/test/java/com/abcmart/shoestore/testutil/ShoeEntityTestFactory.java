package com.abcmart.shoestore.testutil;

import com.abcmart.shoestore.entity.ShoeEntity;
import java.math.BigDecimal;

public class ShoeEntityTestFactory extends TestEntityFactory<ShoeEntity> {

    public static ShoeEntity create(Long id, String name, String color, int size, BigDecimal price) {

        ShoeEntity shoeEntity = ShoeEntity.create(name, color, size, price);
        setId(shoeEntity, "shoeCode", id);
        return shoeEntity;
    }
}
