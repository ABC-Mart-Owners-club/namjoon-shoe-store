package com.abcmart.shoestore.testutil;

import com.abcmart.shoestore.inventory.domain.Inventory;
import java.time.LocalDate;

public class InventoryTestDomainFactory extends TestDomainFactory<Inventory> {

    public static Inventory createInventory(Long shoeCode, LocalDate stockedDate, Long stock) {

        return fixtureMonkey.giveMeBuilder(Inventory.class)
            .set("shoeCode", shoeCode)
            .set("stockedDate", stockedDate)
            .set("stock", stock)
            .sample();
    }

}
