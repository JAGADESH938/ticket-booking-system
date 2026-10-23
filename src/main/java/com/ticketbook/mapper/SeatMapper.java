package com.ticketbook.mapper;

import com.ticketbook.dto.SeatResponse;
import com.ticketbook.entity.Seat;
import org.springframework.stereotype.Component;

@Component
public class SeatMapper {
    public SeatResponse toResponse(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getSeatNumber(),
                seat.getRowNumber(),
                seat.getSeatType(),
                seat.getScreen().getId()
        );
    }
}
