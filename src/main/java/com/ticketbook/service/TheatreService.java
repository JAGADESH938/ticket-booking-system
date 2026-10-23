package com.ticketbook.service;

import com.ticketbook.dto.TheatreRequest;
import com.ticketbook.dto.TheatreResponse;

import java.util.List;

public interface TheatreService {
    TheatreResponse create(TheatreRequest request);
    TheatreResponse update(Long id, TheatreRequest request);
    void delete(Long id);
    TheatreResponse getById(Long id);
    List<TheatreResponse> getAll();
}
