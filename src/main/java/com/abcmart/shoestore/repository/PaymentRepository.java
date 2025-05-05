package com.abcmart.shoestore.repository;

import com.abcmart.shoestore.domain.CardPayment;
import com.abcmart.shoestore.domain.Payment;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository {

    Payment save(Payment payment);

    List<CardPayment> findAllCreditCardPayments();

}
