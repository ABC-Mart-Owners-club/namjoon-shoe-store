package com.abcmart.shoestore.testutil;

import com.abcmart.shoestore.domain.Shoe;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import java.math.BigDecimal;
import net.jqwik.api.Arbitraries;

public class ShoeTestDomainFactory extends TestDomainFactory<Shoe> {

    public static Shoe createShoeByShoeCode(Long shoeCode) {

        return fixtureMonkey.giveMeBuilder(Shoe.class)
            .set("shoeCode", shoeCode)
            .setNotNull("shoeName")
            .setNotNull("color")
            .set("size", Arbitraries.integers().between(240, 300))
            .set("price", Arbitraries.bigDecimals().greaterThan(BigDecimal.ZERO))
            .sample();
    }

    public static Shoe createShoeBy(Long shoeCode, BigDecimal price) {

        return fixtureMonkey.giveMeBuilder(Shoe.class)
            .set("shoeCode", shoeCode)
            .setNotNull("shoeName")
            .setNotNull("color")
            .set("size", Arbitraries.integers().between(240, 300))
            .set("price", price)
            .sample();
    }
}
