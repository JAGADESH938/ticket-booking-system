package com.ticketbook.service.impl;

import com.ticketbook.dto.SeatRequest;
import com.ticketbook.dto.SeatResponse;
import com.ticketbook.entity.Screen;
import com.ticketbook.entity.Seat;
import com.ticketbook.exception.ScreenNotFoundException;
import com.ticketbook.mapper.SeatMapper;
import com.ticketbook.repository.ScreenRepository;
import com.ticketbook.repository.SeatRepository;
import com.ticketbook.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;
    private final SeatMapper seatMapper;

    @Override
    @Transactional
    public SeatResponse create(Long screenId, SeatRequest request) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new ScreenNotFoundException("Screen not found with id: " + screenId));

        Seat seat = new Seat();
        seat.setSeatNumber(request.getSeatNumber());
        seat.setRowNumber(request.getRowNumber());
        seat.setSeatType(request.getSeatType());
        seat.setScreen(screen);

        try {
            Seat saved = seatRepository.save(seat);
            return seatMapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException(
                    "Seat " + request.getRowNumber() + request.getSeatNumber() + " already exists on this screen");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatResponse> getByScreen(Long screenId) {
        return seatRepository.findByScreenId(screenId).stream()
                .map(seatMapper::toResponse)
                .toList();
    }
}
