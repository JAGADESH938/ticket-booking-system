package com.ticketbook.service;

import com.ticketbook.dto.SeatRequest;
import com.ticketbook.dto.SeatResponse;

import java.util.List;

public interface SeatService {
    SeatResponse create(Long screenId, SeatRequest request);
    List<SeatResponse> getByScreen(Long screenId);
}
