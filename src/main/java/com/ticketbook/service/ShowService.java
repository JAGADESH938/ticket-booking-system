package com.ticketbook.service;

import com.ticketbook.dto.SeatAvailabilityResponse;
import com.ticketbook.dto.ShowRequest;
import com.ticketbook.dto.ShowResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ShowService {
    ShowResponse create(ShowRequest request);
    ShowResponse update(Long id, ShowRequest request);
    void cancel(Long id);
    ShowResponse getById(Long id);
    Page<ShowResponse> search(Long movieId, Long theatreId, LocalDate date, Pageable pageable);
    List<SeatAvailabilityResponse> getSeatAvailability(Long showId);
}
