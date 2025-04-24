package com.abcmart.shoestore.repository;

import com.abcmart.shoestore.domain.OrderDetail;
import com.abcmart.shoestore.domain.Order;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository {

    Order save(Order order);

    Order findByOrderNo(Long orderNo);

    List<OrderDetail> findAllNormalStatusOrderDetails();
}
