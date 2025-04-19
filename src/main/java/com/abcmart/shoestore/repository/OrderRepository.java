package com.abcmart.shoestore.repository;

import com.abcmart.shoestore.entity.OrderDetailEntity;
import com.abcmart.shoestore.entity.OrderEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository {

    OrderEntity save(OrderEntity orderEntity);

    OrderEntity findByOrderNo(Long orderNo);

    List<OrderDetailEntity> findAllNormalStatusOrderDetails();
}
