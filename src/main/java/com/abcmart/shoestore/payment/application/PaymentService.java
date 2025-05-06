package com.abcmart.shoestore.payment.application;

import com.abcmart.shoestore.order.application.request.CreateOrderRequest.CreatePaymentRequest;
import com.abcmart.shoestore.payment.domain.CardPayment;
import com.abcmart.shoestore.payment.domain.CashPayment;
import com.abcmart.shoestore.payment.domain.Payment;
import com.abcmart.shoestore.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public List<Payment> createPayment(List<CreatePaymentRequest> requests) {

        List<Payment> paymentList = requests.stream()
            .map(requestedPayment -> {
                if (requestedPayment.getPaymentType().isCash()) {
                    return CashPayment.payInCash(requestedPayment.getPaidAmount());
                }
                return CardPayment.payInCreditCard(
                    requestedPayment.getCreditCardType(),
                    requestedPayment.getPaidAmount()
                );
            })
            .toList();

        return paymentRepository.saveAll(paymentList);
    }

    @Transactional(readOnly = true)
    public List<Payment> findAllByPaymentIds(List<String> paymentIds) {

        return paymentRepository.findAllByIds(paymentIds);
    }

    @Transactional
    public List<Payment> cancelAllByPaymentIds(List<String> paymentIds) {

        List<Payment> payments = paymentRepository.findAllByIds(paymentIds);
        payments.forEach(Payment::updatePaidAmountToZero);
        return paymentRepository.saveAll(payments);
    }

    @Transactional
    public List<Payment> partialCancel(List<String> paymentIds, BigDecimal cancelAmount) {

        List<Payment> payments = paymentRepository.findAllByIds(paymentIds);

        BigDecimal totalCancelAmount = cancelAmount;

        Optional<Payment> availablePayment = payments.stream()
            .filter(payment -> payment.validateAvailableCancel(totalCancelAmount))
            .findFirst();
        if (availablePayment.isPresent()) { // 취소 금액이 하나의 결제수단보다 작아 한번에 취소 가능한 경우

            Payment targetPayment = availablePayment.get();
            targetPayment.partialCancel(totalCancelAmount);

        } else { // 취소 금액이 낱개의 결제수단들보다 커서 나눠서 취소해야하는 경우

            BigDecimal remainCancelAmount = totalCancelAmount;
            for (Payment payment : payments) {
                BigDecimal cancelledAmount = payment.partialCancel(remainCancelAmount);
                remainCancelAmount = remainCancelAmount.subtract(cancelledAmount);
            }
        }
        return paymentRepository.saveAll(payments);
    }
}
