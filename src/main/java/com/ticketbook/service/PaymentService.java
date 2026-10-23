package com.ticketbook.service;

import com.ticketbook.entity.Payment;
import com.ticketbook.entity.PaymentMethod;

import java.math.BigDecimal;

public interface PaymentService {
    Payment processPayment(Long bookingId, BigDecimal amount, PaymentMethod method);
}
