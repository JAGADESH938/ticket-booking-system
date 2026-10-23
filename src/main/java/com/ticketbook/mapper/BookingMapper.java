package com.ticketbook.mapper;

import com.ticketbook.dto.BookingResponse;
import com.ticketbook.entity.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking, String paymentStatus) {
        return new BookingResponse(
                booking.getId(),
                booking.getBookingReference(),
                booking.getShow().getId(),
                booking.getShow().getMovie().getTitle(),
                booking.getBookingTime(),
                booking.getBookingSeats().stream()
                        .map(bs -> bs.getSeat().getRowNumber() + bs.getSeat().getSeatNumber())
                        .toList(),
                booking.getTotalAmount(),
                booking.getBookingStatus().name(),
                paymentStatus
        );
    }
}
