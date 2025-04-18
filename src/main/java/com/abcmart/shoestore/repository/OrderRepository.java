package com.abcmart.shoestore.repository;

import com.abcmart.shoestore.entity.OrderEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository {

    OrderEntity save(OrderEntity orderEntity);

    OrderEntity findByOrderNo(Long orderNo);
}
