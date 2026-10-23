package com.ticketbook.service.impl;

import com.ticketbook.entity.Payment;
import com.ticketbook.entity.PaymentMethod;
import com.ticketbook.entity.PaymentStatus;
import com.ticketbook.repository.PaymentRepository;
import com.ticketbook.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Year;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private static final AtomicLong SEQUENCE = new AtomicLong(1);

    @Override
    public Payment processPayment(Long bookingId, BigDecimal amount, PaymentMethod method) {
        // Simulated gateway call — ~90% success rate, purely for demo purposes.
        boolean success = ThreadLocalRandom.current().nextInt(100) < 90;

        if (!success) {
            return null;
        }

        Payment payment = new Payment();
        payment.setTransactionId(generateTransactionId());
        payment.setAmount(amount);
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        return payment;
    }

    private String generateTransactionId() {
        return "TXN-" + Year.now().getValue() + "-" + String.format("%06d", SEQUENCE.getAndIncrement());
    }
}
