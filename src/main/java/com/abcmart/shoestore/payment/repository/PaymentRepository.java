package com.abcmart.shoestore.payment.repository;

import com.abcmart.shoestore.payment.domain.CardPayment;
import com.abcmart.shoestore.payment.domain.Payment;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository {

    Payment save(Payment payment);

    List<CardPayment> findAllCreditCardPayments();

}
