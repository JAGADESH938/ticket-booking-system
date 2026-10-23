package com.ticketbook.service;

import com.ticketbook.dto.BookingRequest;
import com.ticketbook.dto.BookingResponse;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request);
    BookingResponse getById(Long id);
    List<BookingResponse> getMyBookings();
    void cancelBooking(Long id);
}
