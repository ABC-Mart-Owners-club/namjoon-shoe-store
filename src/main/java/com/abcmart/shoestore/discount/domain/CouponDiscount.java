package com.abcmart.shoestore.discount.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.Getter;

@Getter
public class CouponDiscount extends Discount {

    private final LocalDateTime createdAt;

    public CouponDiscount(int discountRate) {
        super(discountRate);
        createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    @Override
    public DiscountType getDiscountType() {
        return DiscountType.COUPON;
    }

    @Override
    public BigDecimal calculateDiscount() {

        return null;
    }
}
