package com.abcmart.shoestore.coupon.application;

import com.abcmart.shoestore.coupon.domain.Coupon;
import com.abcmart.shoestore.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional(readOnly = true)
    public Coupon findByCode(String code) {

        return couponRepository.findByCode(code)
            .orElseThrow(CouponService::couponNotFoundException);
    }

    private static IllegalArgumentException couponNotFoundException() {

        return new IllegalArgumentException("Coupon not found.");
    }

}
