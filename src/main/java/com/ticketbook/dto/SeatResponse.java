package com.ticketbook.dto;

import com.ticketbook.entity.SeatType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatResponse {
    private Long id;
    private String seatNumber;
    private String rowNumber;
    private SeatType seatType;
    private Long screenId;
}
