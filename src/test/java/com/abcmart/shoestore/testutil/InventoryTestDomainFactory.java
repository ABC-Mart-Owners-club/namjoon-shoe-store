package com.abcmart.shoestore.testutil;

import com.abcmart.shoestore.inventory.domain.Inventory;

public class InventoryTestDomainFactory extends TestDomainFactory<Inventory> {

    public static Inventory createInventory(Long shoeCode, Long stock) {

        return fixtureMonkey.giveMeBuilder(Inventory.class)
            .set("shoeCode", shoeCode)
            .set("stock", stock)
            .sample();
    }

}
