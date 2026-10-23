package com.ticketbook.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ShowRequest {
    @NotNull
    private Long movieId;

    @NotNull
    private Long screenId;

    @NotNull
    @FutureOrPresent(message = "Show date cannot be in the past")
    private LocalDate showDate;

    @NotNull
    private LocalTime startTime;

    @NotNull
    @Positive(message = "Ticket price must be positive")
    private BigDecimal ticketPrice;
}
