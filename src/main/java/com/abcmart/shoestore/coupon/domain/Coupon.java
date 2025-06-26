package com.abcmart.shoestore.coupon.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Coupon {

    private String code;

    private int discountPercent;

    private LocalDateTime createdAt;

    //region constructor
    private Coupon(int discountPercent) {

        this.code = UUID.randomUUID().toString();
        this.discountPercent = discountPercent;
        this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
    //endregion

    public static Coupon create(int discountPercent) {

        return new Coupon(discountPercent);
    }

    public BigDecimal getDiscountRate() {

        return BigDecimal.valueOf(discountPercent)
            .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }
}
