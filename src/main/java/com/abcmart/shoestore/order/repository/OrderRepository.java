package com.abcmart.shoestore.order.repository;

import com.abcmart.shoestore.order.domain.Order;
import com.abcmart.shoestore.order.domain.OrderDetail;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findByOrderNo(Long orderNo);

    List<OrderDetail> findAllNormalStatusOrderDetails();
}
