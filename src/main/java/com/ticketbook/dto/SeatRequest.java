package com.ticketbook.dto;

import com.ticketbook.entity.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeatRequest {
    @NotBlank
    private String seatNumber;

    @NotBlank
    private String rowNumber;

    @NotNull
    private SeatType seatType;
}
