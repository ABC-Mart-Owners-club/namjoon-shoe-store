package com.abcmart.shoestore.repository;

import com.abcmart.shoestore.domain.Order;
import com.abcmart.shoestore.domain.OrderDetail;
import com.abcmart.shoestore.domain.CardPayment;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository {

    Order save(Order order);

    Order findByOrderNo(Long orderNo);

    List<OrderDetail> findAllNormalStatusOrderDetails();

    List<CardPayment> findAllCreditCardPayments();
}
