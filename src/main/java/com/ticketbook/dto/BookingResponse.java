package com.ticketbook.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private String bookingReference;
    private Long showId;
    private String movieTitle;
    private LocalDateTime bookingTime;
    private List<String> seatNumbers;
    private BigDecimal totalAmount;
    private String bookingStatus;
    private String paymentStatus;
}
