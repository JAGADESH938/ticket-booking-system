package com.ticketbook.service;

import com.ticketbook.dto.ScreenRequest;
import com.ticketbook.dto.ScreenResponse;

import java.util.List;

public interface ScreenService {
    ScreenResponse create(Long theatreId, ScreenRequest request);
    ScreenResponse update(Long id, ScreenRequest request);
    void delete(Long id);
    List<ScreenResponse> getByTheatre(Long theatreId);
}
