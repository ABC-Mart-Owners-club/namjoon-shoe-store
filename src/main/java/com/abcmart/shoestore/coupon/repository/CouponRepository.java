package com.abcmart.shoestore.coupon.repository;

import com.abcmart.shoestore.coupon.domain.Coupon;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository {

    Optional<Coupon> findByCode(String code);

}
