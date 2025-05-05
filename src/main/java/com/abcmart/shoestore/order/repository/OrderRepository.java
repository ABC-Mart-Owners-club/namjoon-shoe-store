package com.abcmart.shoestore.order.repository;

import com.abcmart.shoestore.order.domain.Order;
import com.abcmart.shoestore.order.domain.OrderDetail;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository {

    Order save(Order order);

    Order findByOrderNo(Long orderNo);

    List<OrderDetail> findAllNormalStatusOrderDetails();
}
