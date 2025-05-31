package com.abcmart.shoestore.payment.repository;

import com.abcmart.shoestore.payment.domain.CardPayment;
import com.abcmart.shoestore.payment.domain.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository {

    Optional<Payment> findById(Long id);

    List<Payment> findAllByIds(List<String> paymentIds);

    Payment save(Payment payment);

    List<Payment> saveAll(List<Payment> payments);

    List<CardPayment> findAllCreditCardPayments();

}
