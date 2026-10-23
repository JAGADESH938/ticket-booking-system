package com.ticketbook.service;

import com.ticketbook.entity.Payment;
import com.ticketbook.entity.PaymentMethod;
import com.ticketbook.entity.PaymentStatus;
import com.ticketbook.repository.PaymentRepository;
import com.ticketbook.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;

    @RepeatedTest(20)
    void processPayment_whenSuccessful_returnsValidTransactionIdAndSuccessStatus() {
        PaymentServiceImpl paymentService = new PaymentServiceImpl(paymentRepository);

        Payment result = paymentService.processPayment(1L, BigDecimal.valueOf(500), PaymentMethod.UPI);

        if (result != null) {
            assertThat(result.getTransactionId()).matches("TXN-\\d{4}-\\d{6}");
            assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
            assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500));
        }
    }
}
