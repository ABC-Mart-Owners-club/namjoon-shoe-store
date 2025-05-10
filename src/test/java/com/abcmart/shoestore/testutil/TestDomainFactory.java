package com.abcmart.shoestore.testutil;

import com.abcmart.shoestore.payment.domain.CardPayment;
import com.abcmart.shoestore.payment.domain.CashPayment;
import com.abcmart.shoestore.payment.domain.Payment;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.plugin.InterfacePlugin;
import java.util.List;

public abstract class TestDomainFactory<T> {

    protected static final FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
        .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
        .plugin(
            new InterfacePlugin()
                .abstractClassExtends(Payment.class, List.of(CashPayment.class, CardPayment.class))
        )
        .build();

}

